import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService, MicroservicesConfig, MonolithicConfig, LayeredConfig, HexagonalConfig, DddConfig, CqrsConfig, EventDrivenConfig, ModulithConfig } from '../wizard-state.service';

@Component({
  selector: 'app-step5-modules',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Architecture Configuration</h2>
    <p class="subtitle">Configure your {{ archType }} architecture</p>

    @switch (archType) {
      @case ('MICROSERVICES') {
        <div class="tabs">
          <button [class.active]="activeTab === 'services'" (click)="activeTab = 'services'">Services</button>
          <button [class.active]="activeTab === 'communication'" (click)="activeTab = 'communication'">Communication</button>
          <button [class.active]="activeTab === 'resilience'" (click)="activeTab = 'resilience'">Resilience</button>
          <button [class.active]="activeTab === 'infrastructure'" (click)="activeTab = 'infrastructure'">Infrastructure</button>
          <button [class.active]="activeTab === 'observability'" (click)="activeTab = 'observability'">Observability</button>
        </div>

        @switch (activeTab) {
          @case ('services') {
            <div class="section">
              <h3>Microservices Definition</h3>
              <div class="service-form">
                <input type="text" [(ngModel)]="newServiceName" placeholder="Service name (e.g. user-service)">
                <input type="text" [(ngModel)]="newServiceDesc" placeholder="Description">
                <input type="number" [(ngModel)]="newServicePort" placeholder="Port">
                <button (click)="addService()">Add Service</button>
              </div>
              @for (svc of msConfig.services; track svc.name) {
                <div class="service-card">
                  <div class="service-header">
                    <strong>{{ svc.name }}</strong>
                    <span class="port-badge">:{{ svc.port }}</span>
                    <span class="remove" (click)="removeService(svc.name)">&times;</span>
                  </div>
                  <p class="service-desc">{{ svc.description }}</p>
                  <div class="databases-section">
                    <h4>Databases</h4>
                    <div class="db-form">
                      <select [(ngModel)]="newDbType[svc.name]">
                        <option value="">Select database...</option>
                        <option value="POSTGRESQL">PostgreSQL</option>
                        <option value="MYSQL">MySQL</option>
                        <option value="MONGODB">MongoDB</option>
                        <option value="REDIS">Redis</option>
                        <option value="CASSANDRA">Cassandra</option>
                        <option value="NEO4J">Neo4j</option>
                      </select>
                      <select [(ngModel)]="newDbPurpose[svc.name]">
                        <option value="PRIMARY_STORE">Primary Store</option>
                        <option value="CACHE">Cache</option>
                        <option value="SEARCH">Search</option>
                        <option value="EVENT_STORE">Event Store</option>
                      </select>
                      <button (click)="addDatabase(svc.name)">Add DB</button>
                    </div>
                    @for (db of svc.databases; track db.type + db.purpose) {
                      <div class="db-chip">
                        <span class="db-type">{{ db.type }}</span>
                        <span class="db-purpose">({{ db.purpose }})</span>
                        <span class="remove" (click)="removeDatabase(svc.name, db)">&times;</span>
                      </div>
                    }
                  </div>
                </div>
              }
            </div>
          }
          @case ('communication') {
            <div class="section">
              <h3>Synchronous Communication</h3>
              <div class="comm-form">
                <select [(ngModel)]="newSyncSource">
                  <option value="">Source service...</option>
                  @for (svc of msConfig.services; track svc.name) {
                    <option [value]="svc.name">{{ svc.name }}</option>
                  }
                </select>
                <select [(ngModel)]="newSyncProtocol">
                  <option value="REST">REST</option>
                  <option value="GRPC">gRPC</option>
                </select>
                <select [(ngModel)]="newSyncTarget">
                  <option value="">Target service...</option>
                  @for (svc of msConfig.services; track svc.name) {
                    <option [value]="svc.name">{{ svc.name }}</option>
                  }
                </select>
                <button (click)="addSyncComm()">Add</button>
              </div>
              @for (comm of msConfig.syncCommunications; track comm.source + comm.target) {
                <div class="comm-chip">
                  {{ comm.source }} <span class="arrow">→</span> {{ comm.target }} <span class="protocol">[{{ comm.protocol }}]</span>
                  <span class="remove" (click)="removeSyncComm(comm)">&times;</span>
                </div>
              }

              <h3>Asynchronous Communication</h3>
              <div class="comm-form">
                <select [(ngModel)]="newAsyncSource">
                  <option value="">Producer...</option>
                  @for (svc of msConfig.services; track svc.name) {
                    <option [value]="svc.name">{{ svc.name }}</option>
                  }
                </select>
                <select [(ngModel)]="newAsyncBroker">
                  <option value="KAFKA">Kafka</option>
                  <option value="RABBITMQ">RabbitMQ</option>
                </select>
                <select [(ngModel)]="newAsyncTarget">
                  <option value="">Consumer...</option>
                  @for (svc of msConfig.services; track svc.name) {
                    <option [value]="svc.name">{{ svc.name }}</option>
                  }
                </select>
                <input type="text" [(ngModel)]="newAsyncTopic" placeholder="Topic name">
                <select [(ngModel)]="newAsyncSerialization">
                  <option value="JSON">JSON</option>
                  <option value="AVRO">Avro</option>
                  <option value="PROTOBUF">Protobuf</option>
                </select>
                <button (click)="addAsyncComm()">Add</button>
              </div>
              @for (comm of msConfig.asyncCommunications; track comm.source + comm.target + comm.topic) {
                <div class="comm-chip async">
                  {{ comm.source }} <span class="arrow">⤳</span> {{ comm.target }}
                  <span class="protocol">[{{ comm.broker }} / {{ comm.topic }}]</span>
                  <span class="remove" (click)="removeAsyncComm(comm)">&times;</span>
                </div>
              }
            </div>
          }
          @case ('resilience') {
            <div class="section">
              <h3>Resilience Patterns</h3>
              <p class="hint">Configure resilience for each service connection</p>
              @for (comm of msConfig.syncCommunications; track comm.source + comm.target) {
                <div class="resilience-card">
                  <h4>{{ comm.source }} → {{ comm.target }}</h4>
                  <div class="resilience-options">
                    <label><input type="checkbox" [(ngModel)]="resilienceState[comm.source + '_' + comm.target + '_cb']" (change)="updateResilience()"> Circuit Breaker</label>
                    <label><input type="checkbox" [(ngModel)]="resilienceState[comm.source + '_' + comm.target + '_retry']" (change)="updateResilience()"> Retry</label>
                    <label><input type="checkbox" [(ngModel)]="resilienceState[comm.source + '_' + comm.target + '_timeout']" (change)="updateResilience()"> Timeout</label>
                    <label><input type="checkbox" [(ngModel)]="resilienceState[comm.source + '_' + comm.target + '_bulkhead']" (change)="updateResilience()"> Bulkhead</label>
                    <label><input type="checkbox" [(ngModel)]="resilienceState[comm.source + '_' + comm.target + '_ratelimit']" (change)="updateResilience()"> Rate Limit</label>
                  </div>
                </div>
              }
              @if (msConfig.syncCommunications.length === 0) {
                <p class="hint">Add synchronous communications first to configure resilience patterns.</p>
              }
            </div>
          }
          @case ('infrastructure') {
            <div class="section">
              <h3>Service Discovery</h3>
              <div class="radio-group">
                <label><input type="radio" name="discovery" value="EUREKA" [(ngModel)]="msConfig.discovery.type" (change)="saveMsConfig()"> Eureka</label>
                <label><input type="radio" name="discovery" value="CONSUL" [(ngModel)]="msConfig.discovery.type" (change)="saveMsConfig()"> Consul</label>
              </div>

              <h3>API Gateway</h3>
              <label class="toggle"><input type="checkbox" [(ngModel)]="msConfig.gateway.rateLimiting.enabled" (change)="saveMsConfig()"> Enable Rate Limiting</label>

              <h3>Centralized Configuration</h3>
              <label class="toggle"><input type="checkbox" [(ngModel)]="msConfig.centralizedConfig.configServer" (change)="saveMsConfig()"> Spring Cloud Config Server</label>
              <div class="profiles-input">
                <h4>Profiles</h4>
                <div class="chip-list">
                  @for (p of msConfig.centralizedConfig.profiles; track p) {
                    <span class="chip">{{ p }} <span class="remove" (click)="removeProfile(p)">&times;</span></span>
                  }
                </div>
                <div class="inline-form">
                  <input type="text" [(ngModel)]="newProfile" placeholder="Profile name">
                  <button (click)="addProfile()">Add</button>
                </div>
              </div>

              <h3>Secret Management</h3>
              <div class="radio-group">
                <label><input type="radio" name="secrets" value="VAULT" [(ngModel)]="msConfig.centralizedConfig.secretManagement" (change)="saveMsConfig()"> HashiCorp Vault</label>
                <label><input type="radio" name="secrets" value="ENV" [(ngModel)]="msConfig.centralizedConfig.secretManagement" (change)="saveMsConfig()"> Environment Variables</label>
                <label><input type="radio" name="secrets" value="NONE" [(ngModel)]="msConfig.centralizedConfig.secretManagement" (change)="saveMsConfig()"> None</label>
              </div>

              <h3>Orchestration Pattern</h3>
              <div class="radio-group">
                <label><input type="radio" name="saga" value="CHOREOGRAPHY" [(ngModel)]="msConfig.orchestration.sagaPattern" (change)="saveMsConfig()"> Choreography</label>
                <label><input type="radio" name="saga" value="ORCHESTRATION" [(ngModel)]="msConfig.orchestration.sagaPattern" (change)="saveMsConfig()"> Orchestration</label>
                <label><input type="radio" name="saga" value="NONE" [(ngModel)]="msConfig.orchestration.sagaPattern" (change)="saveMsConfig()"> None</label>
              </div>
            </div>
          }
          @case ('observability') {
            <div class="section">
              <h3>Distributed Tracing</h3>
              <div class="radio-group">
                <label><input type="radio" name="tracing" value="ZIPKIN" [(ngModel)]="msConfig.observability.distributedTracing" (change)="saveMsConfig()"> Zipkin</label>
                <label><input type="radio" name="tracing" value="JAEGER" [(ngModel)]="msConfig.observability.distributedTracing" (change)="saveMsConfig()"> Jaeger</label>
                <label><input type="radio" name="tracing" value="NONE" [(ngModel)]="msConfig.observability.distributedTracing" (change)="saveMsConfig()"> None</label>
              </div>

              <h3>Metrics</h3>
              <div class="radio-group">
                <label><input type="radio" name="metrics" value="PROMETHEUS" [(ngModel)]="msConfig.observability.metricsExporter" (change)="saveMsConfig()"> Prometheus</label>
                <label><input type="radio" name="metrics" value="NONE" [(ngModel)]="msConfig.observability.metricsExporter" (change)="saveMsConfig()"> None</label>
              </div>

              <h3>Centralized Logging</h3>
              <div class="radio-group">
                <label><input type="radio" name="logging" value="ELK" [(ngModel)]="msConfig.observability.centralizedLogging" (change)="saveMsConfig()"> ELK Stack</label>
                <label><input type="radio" name="logging" value="LOKI" [(ngModel)]="msConfig.observability.centralizedLogging" (change)="saveMsConfig()"> Grafana Loki</label>
                <label><input type="radio" name="logging" value="NONE" [(ngModel)]="msConfig.observability.centralizedLogging" (change)="saveMsConfig()"> None</label>
              </div>

              <label class="toggle"><input type="checkbox" [(ngModel)]="msConfig.observability.correlationHeaders" (change)="saveMsConfig()"> Auto-propagate Correlation IDs</label>
            </div>
          }
        }
      }
      @case ('MONOLITHIC') {
        <div class="section">
          <h3>Modules</h3>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newModuleName" placeholder="Module name">
            <button (click)="addMonolithModule()">Add Module</button>
          </div>
          @for (mod of monoConfig.modules; track mod.name) {
            <div class="module-chip">{{ mod.name }} <span class="remove" (click)="removeMonolithModule(mod.name)">&times;</span></div>
          }

          <h3>Packaging</h3>
          <div class="radio-group">
            <label><input type="radio" name="packaging" value="JAR" [(ngModel)]="monoConfig.packaging" (change)="saveMonoConfig()"> JAR</label>
            <label><input type="radio" name="packaging" value="WAR" [(ngModel)]="monoConfig.packaging" (change)="saveMonoConfig()"> WAR</label>
          </div>

          <h3>Database</h3>
          <select [(ngModel)]="monoConfig.database.type" (change)="saveMonoConfig()">
            <option value="POSTGRESQL">PostgreSQL</option>
            <option value="MYSQL">MySQL</option>
            <option value="MONGODB">MongoDB</option>
          </select>

          <h3>Caching Strategy</h3>
          <div class="radio-group">
            <label><input type="radio" name="caching" value="REDIS" [(ngModel)]="monoConfig.caching.strategy" (change)="saveMonoConfig()"> Redis</label>
            <label><input type="radio" name="caching" value="CAFFEINE" [(ngModel)]="monoConfig.caching.strategy" (change)="saveMonoConfig()"> Caffeine</label>
            <label><input type="radio" name="caching" value="EHCACHE" [(ngModel)]="monoConfig.caching.strategy" (change)="saveMonoConfig()"> EhCache</label>
            <label><input type="radio" name="caching" value="NONE" [(ngModel)]="monoConfig.caching.strategy" (change)="saveMonoConfig()"> None</label>
          </div>

          <h3>Session Strategy</h3>
          <div class="radio-group">
            <label><input type="radio" name="session" value="STATELESS_JWT" [(ngModel)]="monoConfig.sessionStrategy" (change)="saveMonoConfig()"> Stateless JWT</label>
            <label><input type="radio" name="session" value="STATEFUL_REDIS" [(ngModel)]="monoConfig.sessionStrategy" (change)="saveMonoConfig()"> Stateful (Redis)</label>
            <label><input type="radio" name="session" value="IN_MEMORY" [(ngModel)]="monoConfig.sessionStrategy" (change)="saveMonoConfig()"> In-Memory</label>
          </div>
        </div>
      }
      @case ('LAYERED') {
        <div class="section">
          <h3>Layers</h3>
          <div class="chip-list">
            @for (layer of layeredConfig.layers; track layer) {
              <span class="chip">{{ layer }} <span class="remove" (click)="removeLayer(layer)">&times;</span></span>
            }
          </div>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newLayerName" placeholder="Custom layer name">
            <button (click)="addLayer()">Add Layer</button>
          </div>

          <h3>Cross-Cutting Concerns</h3>
          <div class="checkbox-group">
            <label><input type="checkbox" [(ngModel)]="crossCutting['LOGGING']" (change)="updateLayeredCrossCutting()"> Logging</label>
            <label><input type="checkbox" [(ngModel)]="crossCutting['CACHING']" (change)="updateLayeredCrossCutting()"> Caching</label>
            <label><input type="checkbox" [(ngModel)]="crossCutting['TRANSACTION']" (change)="updateLayeredCrossCutting()"> Transaction Management</label>
            <label><input type="checkbox" [(ngModel)]="crossCutting['AUDIT']" (change)="updateLayeredCrossCutting()"> Audit</label>
            <label><input type="checkbox" [(ngModel)]="crossCutting['PERFORMANCE']" (change)="updateLayeredCrossCutting()"> Performance Monitoring</label>
          </div>

          <h3>DTO Strategy</h3>
          <div class="radio-group">
            <label><input type="radio" name="dto" value="MAPSTRUCT" [(ngModel)]="layeredConfig.dtoStrategy" (change)="saveLayeredConfig()"> MapStruct</label>
            <label><input type="radio" name="dto" value="MODELMAPPER" [(ngModel)]="layeredConfig.dtoStrategy" (change)="saveLayeredConfig()"> ModelMapper</label>
            <label><input type="radio" name="dto" value="MANUAL" [(ngModel)]="layeredConfig.dtoStrategy" (change)="saveLayeredConfig()"> Manual</label>
          </div>

          <h3>Exception Handling</h3>
          <label class="toggle"><input type="checkbox" [(ngModel)]="layeredConfig.exceptionHandling.globalHandler" (change)="saveLayeredConfig()"> Global Exception Handler</label>
        </div>
      }
      @case ('HEXAGONAL') {
        <div class="section">
          <h3>Inbound Ports</h3>
          <div class="checkbox-group">
            <label><input type="checkbox" [(ngModel)]="inboundPorts['REST']" (change)="updateHexPorts()"> REST</label>
            <label><input type="checkbox" [(ngModel)]="inboundPorts['GRPC']" (change)="updateHexPorts()"> gRPC</label>
            <label><input type="checkbox" [(ngModel)]="inboundPorts['GRAPHQL']" (change)="updateHexPorts()"> GraphQL</label>
            <label><input type="checkbox" [(ngModel)]="inboundPorts['CLI']" (change)="updateHexPorts()"> CLI</label>
            <label><input type="checkbox" [(ngModel)]="inboundPorts['MESSAGING']" (change)="updateHexPorts()"> Messaging</label>
          </div>

          <h3>Outbound Ports</h3>
          <div class="checkbox-group">
            <label><input type="checkbox" [(ngModel)]="outboundPorts['DATABASE']" (change)="updateHexPorts()"> Database</label>
            <label><input type="checkbox" [(ngModel)]="outboundPorts['EXTERNAL_API']" (change)="updateHexPorts()"> External API</label>
            <label><input type="checkbox" [(ngModel)]="outboundPorts['MESSAGING']" (change)="updateHexPorts()"> Messaging</label>
            <label><input type="checkbox" [(ngModel)]="outboundPorts['FILE_STORAGE']" (change)="updateHexPorts()"> File Storage</label>
            <label><input type="checkbox" [(ngModel)]="outboundPorts['CACHE']" (change)="updateHexPorts()"> Cache</label>
          </div>

          <h3>Domain Model</h3>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newEntity" placeholder="Entity name">
            <button (click)="addHexEntity()">Add Entity</button>
          </div>
          <div class="chip-list">
            @for (e of hexConfig.domainModel.entities; track e) {
              <span class="chip">{{ e }} <span class="remove" (click)="removeHexEntity(e)">&times;</span></span>
            }
          </div>

          <div class="inline-form">
            <input type="text" [(ngModel)]="newValueObject" placeholder="Value Object name">
            <button (click)="addHexValueObject()">Add VO</button>
          </div>
          <div class="chip-list">
            @for (vo of hexConfig.domainModel.valueObjects; track vo) {
              <span class="chip vo">{{ vo }} <span class="remove" (click)="removeHexValueObject(vo)">&times;</span></span>
            }
          </div>
        </div>
      }
      @case ('DDD') {
        <div class="section">
          <h3>Bounded Contexts</h3>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newContextName" placeholder="Context name">
            <input type="text" [(ngModel)]="newContextDesc" placeholder="Description">
            <button (click)="addBoundedContext()">Add Context</button>
          </div>
          @for (ctx of dddConfig.boundedContexts; track ctx.name) {
            <div class="context-card">
              <strong>{{ ctx.name }}</strong> — {{ ctx.description }}
              <span class="remove" (click)="removeBoundedContext(ctx.name)">&times;</span>
            </div>
          }

          <h3>Context Mapping</h3>
          <div class="comm-form">
            <select [(ngModel)]="newRelSource">
              <option value="">Source...</option>
              @for (ctx of dddConfig.boundedContexts; track ctx.name) {
                <option [value]="ctx.name">{{ ctx.name }}</option>
              }
            </select>
            <select [(ngModel)]="newRelType">
              <option value="SHARED_KERNEL">Shared Kernel</option>
              <option value="CUSTOMER_SUPPLIER">Customer-Supplier</option>
              <option value="ACL">Anti-Corruption Layer</option>
              <option value="CONFORMIST">Conformist</option>
              <option value="OPEN_HOST">Open Host</option>
              <option value="PUBLISHED_LANGUAGE">Published Language</option>
            </select>
            <select [(ngModel)]="newRelTarget">
              <option value="">Target...</option>
              @for (ctx of dddConfig.boundedContexts; track ctx.name) {
                <option [value]="ctx.name">{{ ctx.name }}</option>
              }
            </select>
            <button (click)="addContextRelation()">Add</button>
          </div>
          @for (rel of dddConfig.contextMapping; track rel.source + rel.target) {
            <div class="comm-chip">
              {{ rel.source }} <span class="arrow">→</span> {{ rel.target }} <span class="protocol">[{{ rel.relationType }}]</span>
              <span class="remove" (click)="removeContextRelation(rel)">&times;</span>
            </div>
          }
        </div>
      }
      @case ('CQRS') {
        <div class="section">
          <h3>Write Store</h3>
          <select [(ngModel)]="cqrsConfig.writeStore.databaseType" (change)="saveCqrsConfig()">
            <option value="POSTGRESQL">PostgreSQL</option>
            <option value="MYSQL">MySQL</option>
            <option value="MONGODB">MongoDB</option>
          </select>

          <h3>Read Store</h3>
          <select [(ngModel)]="cqrsConfig.readStore.databaseType" (change)="saveCqrsConfig()">
            <option value="POSTGRESQL">PostgreSQL</option>
            <option value="MYSQL">MySQL</option>
            <option value="MONGODB">MongoDB</option>
            <option value="ELASTICSEARCH">Elasticsearch</option>
          </select>

          <h3>Separation Strategy</h3>
          <div class="radio-group">
            <label><input type="radio" name="separation" value="SAME_DB" [(ngModel)]="cqrsConfig.separationStrategy" (change)="saveCqrsConfig()"> Same Database</label>
            <label><input type="radio" name="separation" value="SEPARATE_DB" [(ngModel)]="cqrsConfig.separationStrategy" (change)="saveCqrsConfig()"> Separate Databases</label>
          </div>

          <h3>Event Store</h3>
          <div class="radio-group">
            <label><input type="radio" name="eventstore" value="AXON" [(ngModel)]="cqrsConfig.eventStore.type" (change)="saveCqrsConfig()"> Axon</label>
            <label><input type="radio" name="eventstore" value="EVENTSTOREDB" [(ngModel)]="cqrsConfig.eventStore.type" (change)="saveCqrsConfig()"> EventStoreDB</label>
            <label><input type="radio" name="eventstore" value="CUSTOM" [(ngModel)]="cqrsConfig.eventStore.type" (change)="saveCqrsConfig()"> Custom</label>
          </div>
        </div>
      }
      @case ('EVENT_DRIVEN') {
        <div class="section">
          <h3>Message Broker</h3>
          <div class="radio-group">
            <label><input type="radio" name="broker" value="KAFKA" [(ngModel)]="eventConfig.broker.type" (change)="saveEventConfig()"> Kafka</label>
            <label><input type="radio" name="broker" value="RABBITMQ" [(ngModel)]="eventConfig.broker.type" (change)="saveEventConfig()"> RabbitMQ</label>
          </div>

          <h3>Events</h3>
          <div class="comm-form">
            <input type="text" [(ngModel)]="newEventName" placeholder="Event name">
            <select [(ngModel)]="newEventFormat">
              <option value="JSON">JSON</option>
              <option value="AVRO">Avro</option>
              <option value="PROTOBUF">Protobuf</option>
            </select>
            <button (click)="addEvent()">Add Event</button>
          </div>
          @for (evt of eventConfig.events; track evt.name) {
            <div class="comm-chip">{{ evt.name }} <span class="protocol">[{{ evt.schemaFormat }}]</span> <span class="remove" (click)="removeEvent(evt.name)">&times;</span></div>
          }

          <h3>Schema Registry</h3>
          <div class="radio-group">
            <label><input type="radio" name="registry" value="CONFLUENT" [(ngModel)]="eventConfig.schemaRegistry.type" (change)="saveEventConfig()"> Confluent</label>
            <label><input type="radio" name="registry" value="APICURIO" [(ngModel)]="eventConfig.schemaRegistry.type" (change)="saveEventConfig()"> Apicurio</label>
            <label><input type="radio" name="registry" value="NONE" [(ngModel)]="eventConfig.schemaRegistry.type" (change)="saveEventConfig()"> None</label>
          </div>

          <h3>Reliability</h3>
          <label class="toggle"><input type="checkbox" [(ngModel)]="eventConfig.reliability.deadLetterQueue" (change)="saveEventConfig()"> Dead Letter Queue</label>
          <label class="toggle"><input type="checkbox" [(ngModel)]="eventConfig.reliability.idempotency" (change)="saveEventConfig()"> Idempotency</label>

          <h3>Stream Processing</h3>
          <label class="toggle"><input type="checkbox" [(ngModel)]="eventConfig.streamProcessing.enabled" (change)="saveEventConfig()"> Enable Stream Processing</label>
          @if (eventConfig.streamProcessing.enabled) {
            <div class="radio-group">
              <label><input type="radio" name="stream" value="KAFKA_STREAMS" [(ngModel)]="eventConfig.streamProcessing.framework" (change)="saveEventConfig()"> Kafka Streams</label>
              <label><input type="radio" name="stream" value="SPRING_CLOUD_STREAM" [(ngModel)]="eventConfig.streamProcessing.framework" (change)="saveEventConfig()"> Spring Cloud Stream</label>
            </div>
          }
        </div>
      }
      @case ('MODULITH') {
        <div class="section">
          <h3>Modules</h3>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newModuleName" placeholder="Module name">
            <input type="text" [(ngModel)]="newModuleResp" placeholder="Responsibility">
            <button (click)="addModulithModule()">Add Module</button>
          </div>
          @for (mod of modulithConfig.modules; track mod.name) {
            <div class="module-chip">{{ mod.name }} — <small>{{ mod.responsibility }}</small> <span class="remove" (click)="removeModulithModule(mod.name)">&times;</span></div>
          }

          <h3>Architecture Tests</h3>
          <label class="toggle"><input type="checkbox" [(ngModel)]="modulithConfig.archTests.enabled" (change)="saveModulithConfig()"> Enable ArchUnit Tests</label>
          @if (modulithConfig.archTests.enabled) {
            <div class="radio-group">
              <label><input type="radio" name="enforce" value="WARN" [(ngModel)]="modulithConfig.archTests.enforcementLevel" (change)="saveModulithConfig()"> Warn</label>
              <label><input type="radio" name="enforce" value="FAIL" [(ngModel)]="modulithConfig.archTests.enforcementLevel" (change)="saveModulithConfig()"> Fail</label>
            </div>
          }
        </div>
      }
      @default {
        <div class="section">
          <h3>Modules</h3>
          <div class="inline-form">
            <input type="text" [(ngModel)]="newModuleName" placeholder="Module name">
            <button (click)="addSimpleModule()">Add Module</button>
          </div>
          <div class="chip-list">
            @for (mod of wizardState.state().architecture.modules; track mod) {
              <span class="chip">{{ mod }} <span class="remove" (click)="removeSimpleModule(mod)">&times;</span></span>
            }
          </div>
        </div>
      }
    }
  `,
  styles: [`
    h2 { margin-bottom: 0.5rem; color: #333; }
    h3 { margin-top: 1.5rem; margin-bottom: 0.5rem; color: #444; }
    h4 { margin-top: 0.75rem; margin-bottom: 0.25rem; color: #555; font-size: 0.9rem; }
    .subtitle { color: #666; margin-bottom: 1.5rem; }
    .tabs { display: flex; gap: 0; border-bottom: 2px solid #e0e0e0; margin-bottom: 1.5rem; }
    .tabs button { padding: 0.75rem 1.25rem; border: none; background: none; cursor: pointer; font-size: 0.9rem; color: #666; border-bottom: 2px solid transparent; margin-bottom: -2px; }
    .tabs button.active { color: #1976d2; border-bottom-color: #1976d2; font-weight: 600; }
    .section { padding: 0.5rem 0; }
    .service-form, .comm-form, .inline-form { display: flex; gap: 0.5rem; margin-bottom: 1rem; flex-wrap: wrap; }
    .service-form input, .service-form select, .comm-form input, .comm-form select, .inline-form input, .inline-form select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .service-form button, .comm-form button, .inline-form button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .service-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; }
    .service-header { display: flex; align-items: center; gap: 0.5rem; }
    .port-badge { background: #e3f2fd; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.85rem; color: #1565c0; }
    .service-desc { color: #666; font-size: 0.9rem; margin: 0.25rem 0; }
    .db-form { display: flex; gap: 0.5rem; margin-top: 0.5rem; }
    .db-form select, .db-form button { padding: 0.4rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.85rem; }
    .db-form button { background: #43a047; color: white; border: none; cursor: pointer; }
    .db-chip { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.3rem 0.7rem; background: #f3e5f5; border-radius: 12px; margin: 0.25rem; font-size: 0.85rem; }
    .db-type { font-weight: 600; color: #6a1b9a; }
    .db-purpose { color: #888; }
    .module-chip, .context-card { display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; background: #e3f2fd; border-radius: 8px; margin-bottom: 0.5rem; }
    .comm-chip { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; background: #e8f5e9; border-radius: 8px; margin: 0.25rem; font-size: 0.9rem; }
    .comm-chip.async { background: #fff3e0; }
    .arrow { font-weight: bold; color: #1976d2; }
    .protocol { color: #888; font-size: 0.85rem; }
    .chip-list { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.5rem; }
    .chip { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.4rem 0.8rem; background: #e3f2fd; border-radius: 16px; font-size: 0.85rem; }
    .chip.vo { background: #fce4ec; }
    .remove { cursor: pointer; color: #c62828; font-weight: bold; }
    .radio-group, .checkbox-group { display: flex; flex-wrap: wrap; gap: 1rem; margin: 0.5rem 0; }
    .radio-group label, .checkbox-group label { display: flex; align-items: center; gap: 0.3rem; cursor: pointer; }
    .toggle { display: flex; align-items: center; gap: 0.5rem; margin: 0.5rem 0; cursor: pointer; }
    .hint { color: #999; font-style: italic; }
    .resilience-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 1rem; margin-bottom: 0.75rem; }
    .resilience-options { display: flex; flex-wrap: wrap; gap: 1rem; margin-top: 0.5rem; }
    .resilience-options label { display: flex; align-items: center; gap: 0.3rem; }
    select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
  `]
})
export class Step5ModulesComponent {
  activeTab = 'services';

  // Microservices state
  msConfig: MicroservicesConfig;
  newServiceName = '';
  newServiceDesc = '';
  newServicePort = 8081;
  newDbType: { [key: string]: string } = {};
  newDbPurpose: { [key: string]: string } = {};
  newSyncSource = '';
  newSyncTarget = '';
  newSyncProtocol = 'REST';
  newAsyncSource = '';
  newAsyncTarget = '';
  newAsyncBroker = 'KAFKA';
  newAsyncTopic = '';
  newAsyncSerialization = 'JSON';
  newProfile = '';
  resilienceState: { [key: string]: boolean } = {};

  // Monolithic state
  monoConfig: MonolithicConfig;

  // Layered state
  layeredConfig: LayeredConfig;
  newLayerName = '';
  crossCutting: { [key: string]: boolean } = {};

  // Hexagonal state
  hexConfig: HexagonalConfig;
  inboundPorts: { [key: string]: boolean } = {};
  outboundPorts: { [key: string]: boolean } = {};
  newEntity = '';
  newValueObject = '';

  // DDD state
  dddConfig: DddConfig;
  newContextName = '';
  newContextDesc = '';
  newRelSource = '';
  newRelTarget = '';
  newRelType = 'SHARED_KERNEL';

  // CQRS state
  cqrsConfig: CqrsConfig;

  // Event-Driven state
  eventConfig: EventDrivenConfig;
  newEventName = '';
  newEventFormat = 'JSON';

  // Modulith state
  modulithConfig: ModulithConfig;
  newModuleName = '';
  newModuleResp = '';

  constructor(public wizardState: WizardStateService) {
    const arch = this.wizardState.state().architecture;

    this.msConfig = arch.microservices || this.defaultMsConfig();
    this.monoConfig = arch.monolithic || this.defaultMonoConfig();
    this.layeredConfig = arch.layered || this.defaultLayeredConfig();
    this.hexConfig = arch.hexagonal || this.defaultHexConfig();
    this.dddConfig = arch.ddd || this.defaultDddConfig();
    this.cqrsConfig = arch.cqrs || this.defaultCqrsConfig();
    this.eventConfig = arch.eventDriven || this.defaultEventConfig();
    this.modulithConfig = arch.modulith || this.defaultModulithConfig();

    if (this.hexConfig.inboundPorts) {
      this.hexConfig.inboundPorts.forEach(p => this.inboundPorts[p] = true);
    }
    if (this.hexConfig.outboundPorts) {
      this.hexConfig.outboundPorts.forEach(p => this.outboundPorts[p] = true);
    }
    if (this.layeredConfig.crossCuttingConcerns) {
      this.layeredConfig.crossCuttingConcerns.forEach(c => this.crossCutting[c] = true);
    }
  }

  get archType(): string {
    return this.wizardState.state().architecture.type;
  }

  // ===== MICROSERVICES =====

  addService(): void {
    if (!this.newServiceName.trim()) return;
    this.msConfig.services.push({
      name: this.newServiceName.trim(),
      description: this.newServiceDesc.trim(),
      port: this.newServicePort,
      databases: []
    });
    this.newServiceName = '';
    this.newServiceDesc = '';
    this.newServicePort = this.msConfig.services.length + 8081;
    this.saveMsConfig();
  }

  removeService(name: string): void {
    this.msConfig.services = this.msConfig.services.filter(s => s.name !== name);
    this.msConfig.syncCommunications = this.msConfig.syncCommunications.filter(c => c.source !== name && c.target !== name);
    this.msConfig.asyncCommunications = this.msConfig.asyncCommunications.filter(c => c.source !== name && c.target !== name);
    this.saveMsConfig();
  }

  addDatabase(serviceName: string): void {
    const type = this.newDbType[serviceName];
    const purpose = this.newDbPurpose[serviceName] || 'PRIMARY_STORE';
    if (!type) return;
    const svc = this.msConfig.services.find(s => s.name === serviceName);
    if (svc) {
      svc.databases.push({ type, purpose, generateEntity: true });
      this.newDbType[serviceName] = '';
      this.newDbPurpose[serviceName] = '';
      this.saveMsConfig();
    }
  }

  removeDatabase(serviceName: string, db: any): void {
    const svc = this.msConfig.services.find(s => s.name === serviceName);
    if (svc) {
      svc.databases = svc.databases.filter(d => d !== db);
      this.saveMsConfig();
    }
  }

  addSyncComm(): void {
    if (!this.newSyncSource || !this.newSyncTarget || this.newSyncSource === this.newSyncTarget) return;
    this.msConfig.syncCommunications.push({
      protocol: this.newSyncProtocol,
      source: this.newSyncSource,
      target: this.newSyncTarget,
      endpoints: [],
      loadBalancing: true
    });
    this.newSyncSource = '';
    this.newSyncTarget = '';
    this.saveMsConfig();
  }

  removeSyncComm(comm: any): void {
    this.msConfig.syncCommunications = this.msConfig.syncCommunications.filter(c => c !== comm);
    this.saveMsConfig();
  }

  addAsyncComm(): void {
    if (!this.newAsyncSource || !this.newAsyncTarget || !this.newAsyncTopic.trim()) return;
    this.msConfig.asyncCommunications.push({
      broker: this.newAsyncBroker,
      source: this.newAsyncSource,
      target: this.newAsyncTarget,
      topic: this.newAsyncTopic.trim(),
      eventType: this.newAsyncTopic.trim().replace(/-/g, '') + 'Event',
      serialization: this.newAsyncSerialization
    });
    this.newAsyncSource = '';
    this.newAsyncTarget = '';
    this.newAsyncTopic = '';
    this.saveMsConfig();
  }

  removeAsyncComm(comm: any): void {
    this.msConfig.asyncCommunications = this.msConfig.asyncCommunications.filter(c => c !== comm);
    this.saveMsConfig();
  }

  updateResilience(): void {
    this.msConfig.resilience = this.msConfig.syncCommunications.map(comm => {
      const key = comm.source + '_' + comm.target;
      return {
        source: comm.source,
        target: comm.target,
        circuitBreaker: { enabled: !!this.resilienceState[key + '_cb'], failureThreshold: 5, waitDurationSeconds: 30, slidingWindowSize: 10 },
        retry: { enabled: !!this.resilienceState[key + '_retry'], maxAttempts: 3, backoffDelayMs: 1000 },
        timeout: { enabled: !!this.resilienceState[key + '_timeout'], durationMs: 5000 },
        bulkhead: { enabled: !!this.resilienceState[key + '_bulkhead'], maxConcurrentCalls: 10 },
        rateLimit: { enabled: !!this.resilienceState[key + '_ratelimit'], limitForPeriod: 100, periodDurationSeconds: 1 }
      };
    });
    this.saveMsConfig();
  }

  addProfile(): void {
    if (!this.newProfile.trim()) return;
    this.msConfig.centralizedConfig.profiles.push(this.newProfile.trim());
    this.newProfile = '';
    this.saveMsConfig();
  }

  removeProfile(p: string): void {
    this.msConfig.centralizedConfig.profiles = this.msConfig.centralizedConfig.profiles.filter(x => x !== p);
    this.saveMsConfig();
  }

  saveMsConfig(): void {
    const arch = this.wizardState.state().architecture;
    const modules = this.msConfig.services.map(s => s.name);
    this.wizardState.updateState({ architecture: { ...arch, modules, microservices: { ...this.msConfig } } });
  }

  // ===== MONOLITHIC =====

  addMonolithModule(): void {
    if (!this.newModuleName.trim()) return;
    this.monoConfig.modules.push({ name: this.newModuleName.trim(), description: '', dependsOn: [] });
    this.newModuleName = '';
    this.saveMonoConfig();
  }

  removeMonolithModule(name: string): void {
    this.monoConfig.modules = this.monoConfig.modules.filter(m => m.name !== name);
    this.saveMonoConfig();
  }

  saveMonoConfig(): void {
    const arch = this.wizardState.state().architecture;
    const modules = this.monoConfig.modules.map(m => m.name);
    this.wizardState.updateState({ architecture: { ...arch, modules, monolithic: { ...this.monoConfig } } });
  }

  // ===== LAYERED =====

  addLayer(): void {
    if (!this.newLayerName.trim()) return;
    this.layeredConfig.layers.push(this.newLayerName.trim());
    this.newLayerName = '';
    this.saveLayeredConfig();
  }

  removeLayer(layer: string): void {
    this.layeredConfig.layers = this.layeredConfig.layers.filter(l => l !== layer);
    this.saveLayeredConfig();
  }

  updateLayeredCrossCutting(): void {
    this.layeredConfig.crossCuttingConcerns = Object.keys(this.crossCutting).filter(k => this.crossCutting[k]);
    this.saveLayeredConfig();
  }

  saveLayeredConfig(): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, layered: { ...this.layeredConfig } } });
  }

  // ===== HEXAGONAL =====

  updateHexPorts(): void {
    this.hexConfig.inboundPorts = Object.keys(this.inboundPorts).filter(k => this.inboundPorts[k]);
    this.hexConfig.outboundPorts = Object.keys(this.outboundPorts).filter(k => this.outboundPorts[k]);
    this.saveHexConfig();
  }

  addHexEntity(): void {
    if (!this.newEntity.trim()) return;
    this.hexConfig.domainModel.entities.push(this.newEntity.trim());
    this.newEntity = '';
    this.saveHexConfig();
  }

  removeHexEntity(e: string): void {
    this.hexConfig.domainModel.entities = this.hexConfig.domainModel.entities.filter(x => x !== e);
    this.saveHexConfig();
  }

  addHexValueObject(): void {
    if (!this.newValueObject.trim()) return;
    this.hexConfig.domainModel.valueObjects.push(this.newValueObject.trim());
    this.newValueObject = '';
    this.saveHexConfig();
  }

  removeHexValueObject(vo: string): void {
    this.hexConfig.domainModel.valueObjects = this.hexConfig.domainModel.valueObjects.filter(x => x !== vo);
    this.saveHexConfig();
  }

  saveHexConfig(): void {
    const arch = this.wizardState.state().architecture;
    const modules = this.hexConfig.domainModel.entities.map(e => e.toLowerCase());
    this.wizardState.updateState({ architecture: { ...arch, modules, hexagonal: { ...this.hexConfig } } });
  }

  // ===== DDD =====

  addBoundedContext(): void {
    if (!this.newContextName.trim()) return;
    this.dddConfig.boundedContexts.push({ name: this.newContextName.trim(), description: this.newContextDesc.trim(), responsibility: '' });
    this.newContextName = '';
    this.newContextDesc = '';
    this.saveDddConfig();
  }

  removeBoundedContext(name: string): void {
    this.dddConfig.boundedContexts = this.dddConfig.boundedContexts.filter(c => c.name !== name);
    this.dddConfig.contextMapping = this.dddConfig.contextMapping.filter(r => r.source !== name && r.target !== name);
    this.saveDddConfig();
  }

  addContextRelation(): void {
    if (!this.newRelSource || !this.newRelTarget || this.newRelSource === this.newRelTarget) return;
    this.dddConfig.contextMapping.push({ source: this.newRelSource, target: this.newRelTarget, relationType: this.newRelType });
    this.newRelSource = '';
    this.newRelTarget = '';
    this.saveDddConfig();
  }

  removeContextRelation(rel: any): void {
    this.dddConfig.contextMapping = this.dddConfig.contextMapping.filter(r => r !== rel);
    this.saveDddConfig();
  }

  saveDddConfig(): void {
    const arch = this.wizardState.state().architecture;
    const modules = this.dddConfig.boundedContexts.map(c => c.name.toLowerCase());
    this.wizardState.updateState({ architecture: { ...arch, modules, ddd: { ...this.dddConfig } } });
  }

  // ===== CQRS =====

  saveCqrsConfig(): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, cqrs: { ...this.cqrsConfig } } });
  }

  // ===== EVENT-DRIVEN =====

  addEvent(): void {
    if (!this.newEventName.trim()) return;
    this.eventConfig.events.push({ name: this.newEventName.trim(), schemaFormat: this.newEventFormat, version: '1.0', fields: [] });
    this.newEventName = '';
    this.saveEventConfig();
  }

  removeEvent(name: string): void {
    this.eventConfig.events = this.eventConfig.events.filter(e => e.name !== name);
    this.saveEventConfig();
  }

  saveEventConfig(): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, eventDriven: { ...this.eventConfig } } });
  }

  // ===== MODULITH =====

  addModulithModule(): void {
    if (!this.newModuleName.trim()) return;
    this.modulithConfig.modules.push({ name: this.newModuleName.trim(), responsibility: this.newModuleResp.trim(), publicApi: [] });
    this.newModuleName = '';
    this.newModuleResp = '';
    this.saveModulithConfig();
  }

  removeModulithModule(name: string): void {
    this.modulithConfig.modules = this.modulithConfig.modules.filter(m => m.name !== name);
    this.saveModulithConfig();
  }

  saveModulithConfig(): void {
    const arch = this.wizardState.state().architecture;
    const modules = this.modulithConfig.modules.map(m => m.name);
    this.wizardState.updateState({ architecture: { ...arch, modules, modulith: { ...this.modulithConfig } } });
  }

  // ===== SIMPLE MODULES (fallback) =====

  addSimpleModule(): void {
    if (!this.newModuleName.trim()) return;
    const arch = this.wizardState.state().architecture;
    if (!arch.modules.includes(this.newModuleName.trim())) {
      this.wizardState.updateState({ architecture: { ...arch, modules: [...arch.modules, this.newModuleName.trim()] } });
    }
    this.newModuleName = '';
  }

  removeSimpleModule(mod: string): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, modules: arch.modules.filter(m => m !== mod) } });
  }

  // ===== DEFAULTS =====

  private defaultMsConfig(): MicroservicesConfig {
    return {
      services: [], syncCommunications: [], asyncCommunications: [], resilience: [],
      discovery: { type: 'EUREKA' },
      gateway: { routes: [], rateLimiting: { enabled: false, replenishRate: 10, burstCapacity: 20 }, authPerRoute: {}, cors: { allowedOrigins: ['*'], allowedMethods: ['GET', 'POST', 'PUT', 'DELETE'], allowedHeaders: ['*'] } },
      orchestration: { sagaPattern: 'NONE', sagas: [] },
      centralizedConfig: { configServer: true, profiles: ['dev', 'staging', 'prod'], secretManagement: 'ENV' },
      sharedModules: [],
      observability: { distributedTracing: 'ZIPKIN', metricsExporter: 'PROMETHEUS', centralizedLogging: 'NONE', correlationHeaders: true }
    };
  }

  private defaultMonoConfig(): MonolithicConfig {
    return {
      modules: [], packaging: 'JAR', profiles: ['dev', 'prod'],
      caching: { strategy: 'NONE', cachedEntities: [] },
      scheduling: { enabled: false, jobs: [] },
      sessionStrategy: 'STATELESS_JWT',
      database: { type: 'POSTGRESQL', poolSize: 10, connectionTimeout: 30000 }
    };
  }

  private defaultLayeredConfig(): LayeredConfig {
    return {
      layers: ['Controller', 'Service', 'Repository'],
      crossCuttingConcerns: [],
      dtoStrategy: 'MAPSTRUCT',
      separateValidationLayer: false,
      exceptionHandling: { globalHandler: true, customErrorCodes: [], errorResponseFormat: 'JSON' },
      modules: []
    };
  }

  private defaultHexConfig(): HexagonalConfig {
    return {
      inboundPorts: ['REST'], outboundPorts: ['DATABASE'],
      useCases: [], adapters: [],
      domainModel: { entities: [], valueObjects: [], domainServices: [] }
    };
  }

  private defaultDddConfig(): DddConfig {
    return {
      boundedContexts: [], contextMapping: [], aggregates: [],
      domainEvents: [], valueObjects: [], domainServices: []
    };
  }

  private defaultCqrsConfig(): CqrsConfig {
    return {
      commands: [], queries: [],
      writeStore: { databaseType: 'POSTGRESQL', schemaStrategy: 'CREATE' },
      readStore: { databaseType: 'POSTGRESQL', schemaStrategy: 'CREATE' },
      eventStore: { type: 'AXON', retentionPolicy: '30d' },
      projections: [], separationStrategy: 'SAME_DB'
    };
  }

  private defaultEventConfig(): EventDrivenConfig {
    return {
      events: [], producers: [], consumers: [],
      broker: { type: 'KAFKA', clusterConfig: {} },
      sagas: [],
      reliability: { deadLetterQueue: true, retryCount: 3, idempotency: true, idempotencyStrategy: 'MESSAGE_ID' },
      schemaRegistry: { type: 'NONE', compatibility: 'BACKWARD' },
      streamProcessing: { enabled: false, framework: 'KAFKA_STREAMS' }
    };
  }

  private defaultModulithConfig(): ModulithConfig {
    return {
      modules: [], allowedDependencies: [], internalEvents: [],
      sharedKernelModules: [],
      archTests: { enabled: true, enforcementLevel: 'FAIL', customRules: [] }
    };
  }
}
