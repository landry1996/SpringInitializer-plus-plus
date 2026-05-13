import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import { SpringForgeClient, ProjectConfiguration } from './client';

export class SpringForgePanel {
    public static currentPanel: SpringForgePanel | undefined;
    private static readonly viewType = 'springforgePanel';

    private readonly panel: vscode.WebviewPanel;
    private readonly extensionUri: vscode.Uri;
    private readonly outputChannel: vscode.OutputChannel;
    private readonly client: SpringForgeClient;
    private disposables: vscode.Disposable[] = [];

    public static createOrShow(extensionUri: vscode.Uri, outputChannel: vscode.OutputChannel) {
        const column = vscode.window.activeTextEditor
            ? vscode.window.activeTextEditor.viewColumn
            : undefined;

        if (SpringForgePanel.currentPanel) {
            SpringForgePanel.currentPanel.panel.reveal(column);
            return;
        }

        const panel = vscode.window.createWebviewPanel(
            SpringForgePanel.viewType,
            'SpringForge: Generate Project',
            column || vscode.ViewColumn.One,
            {
                enableScripts: true,
                retainContextWhenHidden: true,
            }
        );

        SpringForgePanel.currentPanel = new SpringForgePanel(panel, extensionUri, outputChannel);
    }

    private constructor(panel: vscode.WebviewPanel, extensionUri: vscode.Uri, outputChannel: vscode.OutputChannel) {
        this.panel = panel;
        this.extensionUri = extensionUri;
        this.outputChannel = outputChannel;
        this.client = new SpringForgeClient();

        this.panel.webview.html = this.getWebviewContent();
        this.panel.onDidDispose(() => this.dispose(), null, this.disposables);
        this.panel.webview.onDidReceiveMessage(
            async (message) => await this.handleMessage(message),
            null,
            this.disposables
        );
    }

    private async handleMessage(message: any) {
        switch (message.command) {
            case 'generate':
                await this.generateProject(message.config);
                break;
            case 'validate':
                await this.validateConfig(message.config);
                break;
        }
    }

    private async validateConfig(config: ProjectConfiguration) {
        try {
            const result = await this.client.validate(config);
            this.panel.webview.postMessage({ command: 'validationResult', data: result });
        } catch (error: any) {
            this.panel.webview.postMessage({
                command: 'validationResult',
                data: { valid: false, errors: [error.message] }
            });
        }
    }

    private async generateProject(config: ProjectConfiguration) {
        try {
            this.outputChannel.appendLine('Starting project generation...');
            this.panel.webview.postMessage({ command: 'status', data: { status: 'GENERATING', progress: 0 } });

            const response = await this.client.generate(config);
            this.outputChannel.appendLine(`Generation started: ${response.generationId}`);

            await this.pollStatus(response.generationId);
        } catch (error: any) {
            this.outputChannel.appendLine(`Generation failed: ${error.message}`);
            vscode.window.showErrorMessage(`SpringForge: Generation failed - ${error.message}`);
            this.panel.webview.postMessage({ command: 'status', data: { status: 'FAILED', error: error.message } });
        }
    }

    private async pollStatus(generationId: string) {
        const maxAttempts = 60;
        for (let i = 0; i < maxAttempts; i++) {
            await new Promise(resolve => setTimeout(resolve, 2000));
            const status = await this.client.getStatus(generationId);
            this.panel.webview.postMessage({ command: 'status', data: status });

            if (status.status === 'COMPLETED') {
                await this.downloadAndSave(generationId);
                return;
            } else if (status.status === 'FAILED') {
                vscode.window.showErrorMessage(`SpringForge: ${status.error}`);
                return;
            }
        }
        vscode.window.showWarningMessage('SpringForge: Generation timed out');
    }

    private async downloadAndSave(generationId: string) {
        const config = vscode.workspace.getConfiguration('springforge');
        let outputDir = config.get<string>('outputDirectory', '');

        if (!outputDir) {
            const folders = await vscode.window.showOpenDialog({
                canSelectFolders: true,
                canSelectFiles: false,
                canSelectMany: false,
                openLabel: 'Select output directory',
            });
            if (!folders || folders.length === 0) { return; }
            outputDir = folders[0].fsPath;
        }

        try {
            const data = await this.client.download(generationId);
            const zipPath = path.join(outputDir, `project-${generationId.substring(0, 8)}.zip`);
            fs.writeFileSync(zipPath, data);
            this.outputChannel.appendLine(`Project saved to: ${zipPath}`);

            const openAction = await vscode.window.showInformationMessage(
                `Project generated successfully! Saved to ${zipPath}`,
                'Open Folder', 'Open File'
            );

            if (openAction === 'Open Folder') {
                vscode.commands.executeCommand('vscode.openFolder', vscode.Uri.file(outputDir));
            } else if (openAction === 'Open File') {
                vscode.commands.executeCommand('revealFileInOS', vscode.Uri.file(zipPath));
            }
        } catch (error: any) {
            vscode.window.showErrorMessage(`Failed to download: ${error.message}`);
        }
    }

    private getWebviewContent(): string {
        return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SpringForge</title>
    <style>
        body { font-family: var(--vscode-font-family); padding: 20px; color: var(--vscode-foreground); background: var(--vscode-editor-background); }
        .step { display: none; }
        .step.active { display: block; }
        h2 { color: var(--vscode-titleBar-activeForeground); margin-bottom: 16px; }
        label { display: block; margin: 8px 0 4px; font-weight: 500; }
        input, select { width: 100%; padding: 6px 8px; border: 1px solid var(--vscode-input-border); background: var(--vscode-input-background); color: var(--vscode-input-foreground); border-radius: 2px; box-sizing: border-box; }
        .btn { padding: 8px 16px; margin: 4px; cursor: pointer; border: none; border-radius: 4px; }
        .btn-primary { background: var(--vscode-button-background); color: var(--vscode-button-foreground); }
        .btn-primary:hover { background: var(--vscode-button-hoverBackground); }
        .btn-secondary { background: var(--vscode-button-secondaryBackground); color: var(--vscode-button-secondaryForeground); }
        .nav { margin-top: 20px; display: flex; justify-content: space-between; }
        .progress { margin-top: 20px; }
        .progress-bar { height: 4px; background: var(--vscode-progressBar-background); border-radius: 2px; transition: width 0.3s; }
        .checkbox-group { display: flex; flex-wrap: wrap; gap: 8px; margin: 8px 0; }
        .checkbox-group label { display: inline-flex; align-items: center; gap: 4px; margin: 0; }
        .status-msg { margin-top: 12px; padding: 8px; border-radius: 4px; }
        .status-msg.success { background: var(--vscode-testing-iconPassed); color: white; }
        .status-msg.error { background: var(--vscode-testing-iconFailed); color: white; }
    </style>
</head>
<body>
    <h1>SpringForge Project Generator</h1>

    <div id="step1" class="step active">
        <h2>Step 1: Project Metadata</h2>
        <label>Group ID</label><input id="groupId" value="com.example" />
        <label>Artifact ID</label><input id="artifactId" value="demo" />
        <label>Name</label><input id="name" value="Demo Application" />
        <label>Description</label><input id="description" value="" />
        <label>Package Name</label><input id="packageName" value="com.example.demo" />
        <label>Java Version</label>
        <select id="javaVersion"><option>21</option><option>17</option></select>
        <label>Spring Boot Version</label>
        <select id="springBootVersion"><option>3.3.5</option><option>3.2.11</option></select>
        <label>Build Tool</label>
        <select id="buildTool"><option value="MAVEN">Maven</option><option value="GRADLE_KOTLIN">Gradle (Kotlin)</option></select>
    </div>

    <div id="step2" class="step">
        <h2>Step 2: Architecture</h2>
        <label>Architecture Type</label>
        <select id="archType">
            <option value="HEXAGONAL">Hexagonal</option>
            <option value="LAYERED">Layered</option>
            <option value="DDD">Domain-Driven Design</option>
            <option value="CQRS">CQRS</option>
            <option value="MODULITH">Modulith</option>
            <option value="MICROSERVICES">Microservices</option>
        </select>
        <label>Modules (comma separated)</label>
        <input id="modules" value="user,order" placeholder="user,order,product" />
    </div>

    <div id="step3" class="step">
        <h2>Step 3: Dependencies</h2>
        <div class="checkbox-group">
            <label><input type="checkbox" value="spring-boot-starter-web" checked /> Web</label>
            <label><input type="checkbox" value="spring-boot-starter-data-jpa" checked /> JPA</label>
            <label><input type="checkbox" value="spring-boot-starter-data-mongodb" /> MongoDB</label>
            <label><input type="checkbox" value="spring-boot-starter-security" /> Security</label>
            <label><input type="checkbox" value="spring-boot-starter-validation" /> Validation</label>
            <label><input type="checkbox" value="spring-boot-starter-actuator" /> Actuator</label>
            <label><input type="checkbox" value="spring-boot-starter-data-redis" /> Redis</label>
            <label><input type="checkbox" value="spring-boot-starter-websocket" /> WebSocket</label>
        </div>
    </div>

    <div id="step4" class="step">
        <h2>Step 4: Infrastructure</h2>
        <div class="checkbox-group">
            <label><input type="checkbox" id="docker" checked /> Docker</label>
            <label><input type="checkbox" id="dockerCompose" checked /> Docker Compose</label>
            <label><input type="checkbox" id="kubernetes" /> Kubernetes</label>
            <label><input type="checkbox" id="helm" /> Helm</label>
        </div>
        <label>CI/CD</label>
        <select id="ci"><option value="">None</option><option value="GITHUB_ACTIONS">GitHub Actions</option><option value="GITLAB_CI">GitLab CI</option></select>
    </div>

    <div id="step5" class="step">
        <h2>Step 5: Generate</h2>
        <p>Review your configuration and click Generate to create the project.</p>
        <div id="statusArea"></div>
        <div class="progress" id="progressArea" style="display:none">
            <div class="progress-bar" id="progressBar" style="width:0%"></div>
        </div>
    </div>

    <div class="nav">
        <button class="btn btn-secondary" id="prevBtn" onclick="prevStep()" style="display:none">Previous</button>
        <button class="btn btn-primary" id="nextBtn" onclick="nextStep()">Next</button>
        <button class="btn btn-primary" id="generateBtn" onclick="generate()" style="display:none">Generate Project</button>
    </div>

    <script>
        const vscode = acquireVsCodeApi();
        let currentStep = 1;
        const totalSteps = 5;

        function showStep(step) {
            document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
            document.getElementById('step' + step).classList.add('active');
            document.getElementById('prevBtn').style.display = step > 1 ? 'inline' : 'none';
            document.getElementById('nextBtn').style.display = step < totalSteps ? 'inline' : 'none';
            document.getElementById('generateBtn').style.display = step === totalSteps ? 'inline' : 'none';
        }

        function nextStep() { if (currentStep < totalSteps) { currentStep++; showStep(currentStep); } }
        function prevStep() { if (currentStep > 1) { currentStep--; showStep(currentStep); } }

        function getConfig() {
            const deps = [];
            document.querySelectorAll('#step3 input[type=checkbox]:checked').forEach(cb => deps.push(cb.value));
            return {
                metadata: {
                    groupId: document.getElementById('groupId').value,
                    artifactId: document.getElementById('artifactId').value,
                    name: document.getElementById('name').value,
                    description: document.getElementById('description').value,
                    packageName: document.getElementById('packageName').value,
                    javaVersion: document.getElementById('javaVersion').value,
                    springBootVersion: document.getElementById('springBootVersion').value,
                    buildTool: document.getElementById('buildTool').value,
                },
                architecture: {
                    type: document.getElementById('archType').value,
                    modules: document.getElementById('modules').value.split(',').map(m => m.trim()).filter(Boolean),
                    enableCQRS: false,
                    enableEventSourcing: false,
                },
                dependencies: deps,
                security: null,
                infrastructure: {
                    docker: document.getElementById('docker').checked,
                    dockerCompose: document.getElementById('dockerCompose').checked,
                    kubernetes: document.getElementById('kubernetes').checked,
                    helm: document.getElementById('helm').checked,
                    ci: document.getElementById('ci').value || null,
                },
                messaging: null, observability: null, testing: null, multiTenant: null, options: null,
            };
        }

        function generate() {
            vscode.postMessage({ command: 'generate', config: getConfig() });
            document.getElementById('generateBtn').disabled = true;
            document.getElementById('statusArea').innerHTML = '<p>Generating project...</p>';
            document.getElementById('progressArea').style.display = 'block';
        }

        window.addEventListener('message', event => {
            const msg = event.data;
            if (msg.command === 'status') {
                const s = msg.data;
                document.getElementById('progressBar').style.width = (s.progress || 0) + '%';
                if (s.status === 'COMPLETED') {
                    document.getElementById('statusArea').innerHTML = '<div class="status-msg success">Project generated successfully!</div>';
                } else if (s.status === 'FAILED') {
                    document.getElementById('statusArea').innerHTML = '<div class="status-msg error">Failed: ' + (s.error || 'Unknown error') + '</div>';
                    document.getElementById('generateBtn').disabled = false;
                }
            }
        });
    </script>
</body>
</html>`;
    }

    public dispose() {
        SpringForgePanel.currentPanel = undefined;
        this.panel.dispose();
        while (this.disposables.length) {
            const d = this.disposables.pop();
            if (d) { d.dispose(); }
        }
    }
}
