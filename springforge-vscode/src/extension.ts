import * as vscode from 'vscode';
import { SpringForgePanel } from './panel';
import { SpringForgeClient } from './client';

export function activate(context: vscode.ExtensionContext) {
    const outputChannel = vscode.window.createOutputChannel('SpringForge');
    outputChannel.appendLine('SpringForge extension activated');

    const statusBarItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    statusBarItem.text = '$(rocket) SpringForge';
    statusBarItem.command = 'springforge.generate';
    statusBarItem.tooltip = 'Generate a Spring Boot project';
    statusBarItem.show();
    context.subscriptions.push(statusBarItem);

    const generateCommand = vscode.commands.registerCommand('springforge.generate', () => {
        SpringForgePanel.createOrShow(context.extensionUri, outputChannel);
    });

    const configureCommand = vscode.commands.registerCommand('springforge.configure', () => {
        vscode.commands.executeCommand('workbench.action.openSettings', 'springforge');
    });

    context.subscriptions.push(generateCommand, configureCommand, outputChannel);
}

export function deactivate() {}
