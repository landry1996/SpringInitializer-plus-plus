/**
 * Module: ${moduleName}
 *
 * Architecture: Hexagonal (Ports & Adapters)
 *
 * Packages:
 * - domain: Core business logic, entities, and port interfaces
 * - application: Use cases orchestrating domain operations
 * - infrastructure: Adapters implementing domain ports (JPA, external services)
 * - api: Inbound adapters (REST controllers)
 */
package ${packageName}.${moduleName};
