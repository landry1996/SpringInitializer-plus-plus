import axios, { AxiosInstance } from 'axios';
import * as vscode from 'vscode';

export interface ProjectConfiguration {
    metadata: {
        groupId: string;
        artifactId: string;
        name: string;
        description: string;
        packageName: string;
        javaVersion: string;
        springBootVersion: string;
        buildTool: string;
    };
    architecture: {
        type: string;
        modules: string[];
        enableCQRS: boolean;
        enableEventSourcing: boolean;
    };
    dependencies: string[];
    security: { type: string; roles: string[] } | null;
    infrastructure: {
        docker: boolean;
        dockerCompose: boolean;
        kubernetes: boolean;
        helm: boolean;
        ci: string | null;
    } | null;
    messaging: { type: string; topics: any[] } | null;
    observability: { enabled: boolean; metrics: boolean; tracing: boolean; structuredLogging: boolean } | null;
    testing: { enabled: boolean; testcontainers: boolean; archunit: boolean; contractTesting: boolean } | null;
    multiTenant: { enabled: boolean; strategy: string } | null;
    options: { includeExamples: boolean; formatCode: boolean; runCompileCheck: boolean; outputFormat: string } | null;
}

export interface GenerationResponse {
    generationId: string;
    status: string;
}

export interface GenerationStatus {
    id: string;
    status: string;
    progress: number;
    downloadUrl?: string;
    error?: string;
}

export class SpringForgeClient {
    private client: AxiosInstance;

    constructor() {
        const config = vscode.workspace.getConfiguration('springforge');
        const baseURL = config.get<string>('serverUrl', 'http://localhost:8080');
        const apiKey = config.get<string>('apiKey', '');

        this.client = axios.create({
            baseURL,
            headers: apiKey ? { Authorization: `Bearer ${apiKey}` } : {},
            timeout: 30000,
        });
    }

    async validate(config: ProjectConfiguration): Promise<{ valid: boolean; errors?: string[] }> {
        const response = await this.client.post('/api/v1/projects/validate', { configuration: config });
        return response.data;
    }

    async generate(config: ProjectConfiguration): Promise<GenerationResponse> {
        const response = await this.client.post('/api/v1/projects/generate', { configuration: config });
        return response.data;
    }

    async getStatus(generationId: string): Promise<GenerationStatus> {
        const response = await this.client.get(`/api/v1/generations/${generationId}/status`);
        return response.data;
    }

    async download(generationId: string): Promise<Buffer> {
        const response = await this.client.get(`/api/v1/generations/${generationId}/download`, {
            responseType: 'arraybuffer',
        });
        return Buffer.from(response.data);
    }

    async getVersions(): Promise<string[]> {
        const response = await this.client.get('/api/v1/versions');
        return response.data;
    }

    async getDependencies(): Promise<any[]> {
        const response = await this.client.get('/api/v1/dependencies');
        return response.data;
    }
}
