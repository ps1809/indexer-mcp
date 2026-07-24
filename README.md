# ProjectIQ MCP Server

A standalone MCP (Model Context Protocol) server that exposes ProjectIQ Indexer capabilities as MCP tools for AI coding agents like Cline.

## Overview

ProjectIQ MCP Server bridges the gap between **ProjectIQ Indexer** (a code intelligence engine) and **AI coding agents** (like Cline) by implementing the Model Context Protocol (MCP). This allows AI agents to query repository structure, search code, find Spring components, inspect dependencies, build rich development context, and execute intelligent developer workflows — all through standardised MCP tool calls.

### Phase 2: Intelligent Developer Workflow

Phase 2 introduces a **deterministic development workflow** that guides AI coding agents through a structured, multi-step analysis pipeline. Instead of requiring agents to manually chain individual tool calls, Phase 2 provides high-level intelligent tools that automate task analysis, context assembly, impact evaluation, implementation planning, test analysis, refactoring guidance, architecture insights, and repository health evaluation.

```
Natural Language Request
         ↓
  [analyze_task] → Task Analysis
         ↓
  [assemble_context] → Context Assembly
         ↓
  [analyze_impact] → Impact Analysis
         ↓
  [implementation_plan] → Implementation Planning
         ↓
  [test_impact_analysis] → Test Impact Analysis
         ↓
  [refactoring_assistant] → Refactoring Guidance
         ↓
  [architecture_insights] → Architecture Insights
         ↓
  [repository_health] → Repository Health
         ↓
  [repository_conventions] → Repository Conventions
         ↓
      Final Response
```

### Architecture

```
+-------------------+          MCP/HTTP          +-------------------+
|                   |   <───────────────────>    |                   |
|   Cline (AI       |                            |   ProjectIQ MCP   |
|   Coding Agent)   |                            |   Server           |
|                   |                            |   (port 8080)      |
+-------------------+                            +-------------------+
                                                         |
                                               REST/HTTP |
                                                         v
                                                +-------------------+
                                                |                   |
                                                |  ProjectIQ        |
                                                |  Indexer          |
                                                |  (port 8081)      |
                                                |                   |
                                                +-------------------+
```

**Components:**

- **Cline**: AI coding agent that discovers and invokes MCP tools
- **ProjectIQ MCP Server**: Spring Boot application exposing repository intelligence as MCP tools and intelligent developer workflows
- **ProjectIQ Indexer**: REST API providing code analysis, search, and repository metadata

## Prerequisites

- **Java 21** (JDK 21+)
- **Maven 3.8+** (for building from source)
- **ProjectIQ Indexer** running and accessible (default: `http://localhost:8081`)

## Build Instructions

```bash
# Clone the repository
git clone https://github.com/ps1809/indexer-mcp.git
cd indexer-mcp

# Build the project (includes compilation, tests, and packaging)
mvn clean install
```

The build produces:
- `target/projectiq-mcp-server-1.0.0-SNAPSHOT.jar` — executable Spring Boot JAR

## Running Locally

### 1. Start ProjectIQ Indexer

Ensure the ProjectIQ Indexer is running on `http://localhost:8081` (or configure a different URL — see [Configuration](#configuration)).

### 2. Start the MCP Server

```bash
# From the project root
mvn spring-boot:run
```

Or using the packaged JAR:

```bash
java -jar target/projectiq-mcp-server-1.0.0-SNAPSHOT.jar
```

The server starts on `http://localhost:8080`.

### 3. Verify Startup

Check the application logs for:

```
ProjectIQ MCP Server started successfully
Server is ready to accept MCP tool requests
```

## Configuration

All configuration is in `src/main/resources/application.yml`.

### Indexer Connection

```yaml
projectiq:
  indexer:
    base-url: http://localhost:8081      # Indexer REST API base URL
    connect-timeout: 5000                # Connection timeout (ms)
    read-timeout: 30000                  # Read timeout (ms)
```

### Response Cache

```yaml
projectiq:
  cache:
    enabled: true                        # Enable/disable caching
    ttl-seconds: 300                     # Cache TTL (seconds)
    max-size: 1000                       # Maximum cache entries
```

### MCP Server

```yaml
spring.ai:
  mcp:
    server:
      name: projectiq-mcp-server
      version: 1.0.0
      type: SYNC                         # Synchronous execution
      stdio: true                        # stdio transport for MCP
```

### Logging

```yaml
logging:
  level:
    com.projectiq.mcp: INFO              # MCP tool logging
    org.springframework.ai: DEBUG        # MCP framework logging
```

## Connecting to ProjectIQ Indexer

1. Ensure the Indexer is running on the configured `base-url` (default: `http://localhost:8081`)
2. The MCP Server verifies connectivity on startup via `/actuator/health` endpoint
3. If the Indexer is unreachable, tools return appropriate error messages

## Available MCP Tools

The server registers the following MCP tools for AI agent discovery:

### Core Tools

| Tool | Description |
|------|-------------|
| `ping` | Verify MCP server connectivity. Returns `"pong"`. |

### Repository Intelligence

| Tool | Description |
|------|-------------|
| `repository_summary` | Get an overview of a repository (packages, classes, structure) |
| `repository_statistics` | Get repository statistics (file types, contributors, counts) |
| `search_code` | Search code across the repository |

### Spring Framework Analysis

| Tool | Description |
|------|-------------|
| `find_spring_component` | Find Spring components (Service, Repository, Controller, etc.) |
| `find_rest_api` | Find REST API endpoints and their HTTP method mappings |

### Code Inspection

| Tool | Description |
|------|-------------|
| `find_dependency` | Find dependencies (Maven, Gradle, internal modules, external libraries) |
| `find_class` | Find Java classes by name, type, or package |
| `find_method` | Find Java methods by name, type, or package |

### File & Context

| Tool | Description |
|------|-------------|
| `list_related_files` | Find files related to a given class, method, or component |
| `build_context` | Build a structured context summary for a repository |
| `development_context` | Get development context with recent changes and active work |
| `prompt_context` | Get prompt-friendly context for AI model interactions |

### Phase 2: Intelligent Developer Workflow

| Tool | Description |
|------|-------------|
| `analyze_task` | Analyze development requests, classifying task type, identifying entities involved, and estimating complexity |
| `assemble_context` | Assemble relevant repository context for a task, including related files, classes, methods, and dependencies |
| `analyze_impact` | Evaluate the impact of proposed changes, identifying scope, risk, affected components, and dependencies |
| `implementation_plan` | Generate a deterministic implementation plan with ordered steps, files to modify, testing scope, and risks |
| `test_impact_analysis` | Analyze which tests are affected by proposed changes and recommend testing strategies |
| `refactoring_assistant` | Provide refactoring recommendations with goals, steps, affected components, risks, and testing strategies |
| `architecture_insights` | Extract architecture patterns, layering, dependency flows, and design characteristics from repository code |
| `repository_health` | Assess repository health including code quality, test coverage, documentation, and dependency freshness |
| `repository_conventions` | Discover repository conventions including naming patterns, code styles, testing frameworks, and documentation practices |

### Workflow Integration Services

| Service | Description |
|---------|-------------|
| `TaskAnalysisService` | Analyzes natural language development requests to determine task type, entities, complexity, and confidence |
| `ContextAssemblyService` | Assembles relevant repository context by identifying related files, classes, methods, and dependencies |
| `ImpactAnalysisService` | Evaluates scope, risk level, affected components, and dependencies for proposed changes |
| `ImplementationPlanningService` | Produces a structured implementation plan with steps, files, validation, and risk mitigation |
| `TestImpactAnalysisService` | Identifies affected test classes, missing tests, test execution order, and effort estimation |
| `RefactoringAssistantService` | Generates refactoring opportunities with goals, steps, risks, and testing strategies |
| `ArchitectureInsightsService` | Reveals architecture patterns, layering violations, cyclic dependencies, and design characteristics |
| `RepositoryHealthService` | Assesses code quality metrics, test coverage, documentation status, and dependency freshness |
| `RepositoryConventionAnalyzerService` | Discovers naming conventions, code patterns, testing frameworks, and documentation standards |

## Example MCP Requests

### Ping (Connectivity Check)

```json
{
  "name": "ping",
  "arguments": {
    "message": "hello"
  }
}
```

**Response:** `"pong"`

### Repository Summary

```json
{
  "name": "repository_summary",
  "arguments": {
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Search Code

```json
{
  "name": "search_code",
  "arguments": {
    "repositoryName": "my-project",
    "query": "findUser",
    "branch": "main",
    "maxResults": 10
  }
}
```

### Find Spring Components

```json
{
  "name": "find_spring_component",
  "arguments": {
    "repositoryName": "my-project",
    "componentTypes": ["Service", "Repository"],
    "packageName": "com.example.service"
  }
}
```

### Build Context

```json
{
  "name": "build_context",
  "arguments": {
    "repositoryName": "my-project",
    "branch": "main",
    "depth": "full",
    "includeSource": true
  }
}
```

### Analyze Task (Phase 2)

```json
{
  "name": "analyze_task",
  "arguments": {
    "task": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Assemble Context (Phase 2)

```json
{
  "name": "assemble_context",
  "arguments": {
    "task": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Analyze Impact (Phase 2)

```json
{
  "name": "analyze_impact",
  "arguments": {
    "task": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Implementation Plan (Phase 2)

```json
{
  "name": "implementation_plan",
  "arguments": {
    "task": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Test Impact Analysis (Phase 2)

```json
{
  "name": "test_impact_analysis",
  "arguments": {
    "task": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Refactoring Assistant (Phase 2)

```json
{
  "name": "refactoring_assistant",
  "arguments": {
    "description": "Extract UserService interface from implementation class",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Architecture Insights (Phase 2)

```json
{
  "name": "architecture_insights",
  "arguments": {
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Repository Health (Phase 2)

```json
{
  "name": "repository_health",
  "arguments": {
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

### Repository Conventions (Phase 2)

```json
{
  "name": "repository_conventions",
  "arguments": {
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

## Phase 3: Intelligent AI Agent Orchestration

Phase 3 introduces a complete **AI Agent Orchestration Platform** that integrates all Phase 2 analysis services into a cohesive, deterministic workflow pipeline. This phase adds workflow orchestration, execution management, intelligent context pipelines, execution planning, validation, recommendations, readiness assessment, development session management, and AI agent handoff capabilities.

### Phase 3 Architecture

```
Developer Request
         ↓
  [Task Analysis] ──→ Task type, complexity, entities
         ↓
  [Workflow Orchestration] ──→ Deterministic workflow execution
         ↓
  [Context Pipeline] ──→ Prioritized, deduplicated context
         ↓
  [Execution Planning] ──→ Dependency-ordered execution roadmap
         ↓
  [Workflow Validation] ──→ 8-category validation pipeline
         ↓
  [Recommendation Engine] ──→ Prioritized recommendations
         ↓
  [Readiness Assessment] ──→ Implementation readiness score
         ↓
  [Development Session] ──→ Session lifecycle management
         ↓
  [Agent Handoff] ──→ Self-contained handoff package
         ↓
  AI Coding Agent
```

### Phase 3 Services

| Service | Description |
|---------|-------------|
| `WorkflowOrchestratorService` | Orchestrates intelligent workflows by coordinating existing repository intelligence services into a single deterministic execution pipeline |
| `WorkflowExecutionService` | Manages workflow execution state, progress tracking, dependency validation, and execution timeline |
| `IntelligentContextPipelineService` | Collects, deduplicates, prioritizes, and filters repository intelligence into an optimized AI-ready context package |
| `ExecutionPlanningService` | Generates deterministic execution roadmaps with dependency validation, topological ordering, risk assessment, and effort estimation |
| `WorkflowValidationService` | Validates workflows across 8 categories: Workflow, Repository, Dependency, Architecture, Convention, Test Coverage, Risk, and Execution Readiness |
| `RecommendationEngineService` | Generates deterministic, prioritized recommendations from workflow results, validation reports, and repository intelligence |
| `ExecutionReadinessService` | Consolidates planning, validation, recommendations, and intelligence into a single readiness decision with implementation recommendation |
| `DevelopmentSessionService` | Manages the complete lifecycle of AI-assisted development sessions with progress tracking and state preservation |
| `AgentHandoffService` | Generates self-contained, integrity-verified handoff packages for seamless AI agent transitions |

### Phase 3 MCP Tools

| Tool | Description |
|------|-------------|
| `orchestrate_workflow` | Orchestrate a complete workflow from a developer request, coordinating all analysis services |
| `execute_workflow` | Execute a workflow definition with progress tracking and dependency validation |
| `build_context_pipeline` | Build an optimized, prioritized context pipeline from all repository intelligence sources |
| `plan_execution` | Generate a deterministic execution roadmap with dependency-ordered tasks and risk assessment |
| `validate_workflow` | Validate a workflow across 8 categories with readiness scoring and blocking issue detection |
| `generate_recommendations` | Generate prioritized, deterministic recommendations from workflow analysis and repository intelligence |
| `assess_execution_readiness` | Assess execution readiness with category-level scoring and final implementation recommendation |
| `create_development_session` | Create a new development session for tracking workflow progress |
| `get_development_session` | Load an existing development session by ID |
| `resume_development_session` | Resume an interrupted development session |
| `complete_development_session` | Complete a development session with final state |
| `export_agent_handoff` | Export a self-contained handoff package for AI agent transition |
| `import_agent_handoff` | Import a handoff package and restore development session state |
| `execute_end_to_end_workflow` | Execute the complete end-to-end workflow integrating all Phase 3 services |

### End-to-End Workflow

The `execute_end_to_end_workflow` tool provides a single-call integration of all Phase 3 services:

```json
{
  "name": "execute_end_to_end_workflow",
  "arguments": {
    "request": "Add pagination to UserController",
    "repositoryName": "my-project",
    "branch": "main"
  }
}
```

**Response includes all stages:**
- Task Analysis results
- Workflow Orchestration results
- Context Package (prioritized, deduplicated)
- Execution Plan (dependency-ordered tasks)
- Validation Report (8-category validation)
- Recommendation Report (prioritized recommendations)
- Readiness Report (implementation readiness score)
- Development Session (session tracking)
- Agent Handoff Package (self-contained JSON for AI agent)

### Development Session Lifecycle

```
CREATED → IN_PROGRESS → COMPLETED → ARCHIVED
              ↑              |
              └──────────────┘
              (resume)
```

### Agent Handoff Package

The handoff package is a self-contained JSON artifact containing:
- Complete session state and workflow progress
- Repository intelligence and context
- Validation results and recommendations
- Execution history (immutable)
- Integrity hash (SHA-256) for corruption detection
- Suggested next actions for the receiving agent

### Intelligent Workflow Pipeline

The complete Phase 3 pipeline follows this deterministic flow:

1. **Developer Request** → Natural language development task
2. **Task Analysis** → Classify task type, identify entities, estimate complexity
3. **Workflow Generation** → Build deterministic workflow definition with ordered steps
4. **Context Assembly** → Gather repository intelligence from all sources
5. **Execution Planning** → Generate dependency-ordered execution roadmap
6. **Workflow Validation** → Validate across 8 categories with readiness scoring
7. **Recommendation Generation** → Generate prioritized, deterministic recommendations
8. **Readiness Assessment** → Assess implementation readiness with final recommendation
9. **Development Session** → Create and manage session lifecycle
10. **Agent Handoff** → Export self-contained handoff package

All stages are fully deterministic with no AI/LLM involvement. Each stage produces stable, repeatable results.

## Troubleshooting

### Server Won't Start

1. **Port conflict**: Ensure port 8080 is not in use. Configure a different port in `application.yml`.
2. **Java version**: Verify Java 21 is installed: `java -version`
3. **Missing dependencies**: Run `mvn clean install -U` to refresh dependencies

### Indexer Communication Errors

1. **Connection refused**: Ensure Indexer is running on the configured `base-url`
2. **Timeouts**: Increase `connect-timeout` or `read-timeout` in configuration
3. **Null responses**: Check Indexer logs for errors processing requests

### MCP Tool Discovery Fails

1. Verify the server started successfully by checking logs
2. Ensure Cline is configured to connect to this MCP server
3. Check that `stdio: true` is set in configuration

### Cache Issues

1. **Stale data**: Reduce `ttl-seconds` in cache configuration
2. **Memory issues**: Reduce `max-size` in cache configuration
3. **Debug caching**: Set `com.projectiq.mcp.cache: DEBUG` in logging config

### Phase 2 Tool Issues

1. **Missing tools**: Verify `McpServerConfig.java` registers all Phase 2 tools (see [Build Instructions](#build-instructions))
2. **Deterministic responses**: Phase 2 tools provide structured JSON. If response format is unexpected, check the Indexer is healthy
3. **Workflow ordering**: The recommended workflow sequence ensures optimal context propagation between tools
4. **Empty context**: If `assemble_context` returns empty results, the repository may not be fully indexed by ProjectIQ Indexer

## Project Structure

```
src/
├── main/
│   ├── java/com/projectiq/mcp/
│   │   ├── ProjectIqMcpServerApplication.java    # Application entry point
│   │   ├── analysis/                             # Phase 2: Intelligent Developer Workflow
│   │   │   ├── dto/                              # Analysis DTOs
│   │   │   │   ├── TaskType.java                 # Development task type enum
│   │   │   │   ├── ConfidenceLevel.java          # Confidence level enum
│   │   │   │   ├── ComplexityLevel.java          # Complexity level enum
│   │   │   │   ├── TaskAnalysisResponse.java     # Task analysis response DTO
│   │   │   │   ├── ContextAssemblyResponse.java  # Context assembly response DTO
│   │   │   │   ├── ScopeLevel.java              # Impact scope level enum
│   │   │   │   ├── RiskLevel.java               # Risk level enum
│   │   │   │   ├── ImpactAnalysisResponse.java  # Impact analysis response DTO
│   │   │   │   ├── ImplementationPlanningResponse.java  # Implementation plan response DTO
│   │   │   │   ├── TestImpactAnalysisResponse.java      # Test impact analysis response DTO
│   │   │   │   ├── RefactoringAssistantResponse.java    # Refactoring assistant response DTO
│   │   │   │   ├── ArchitectureInsightsResponse.java    # Architecture insights response DTO
│   │   │   │   ├── RepositoryHealthResponse.java        # Repository health response DTO
│   │   │   │   └── RepositoryConventionResponse.java    # Repository convention response DTO
│   │   │   └── service/                          # Analysis service implementations
│   │   │       ├── TaskAnalysisService.java
│   │   │       ├── ContextAssemblyService.java
│   │   │       ├── ImpactAnalysisService.java
│   │   │       ├── ImplementationPlanningService.java
│   │   │       ├── TestImpactAnalysisService.java
│   │   │       ├── RefactoringAssistantService.java
│   │   │       ├── ArchitectureInsightsService.java
│   │   │       ├── RepositoryHealthService.java
│   │   │       └── RepositoryConventionAnalyzerService.java
│   │   ├── config/
│   │   │   └── McpServerConfig.java              # MCP tool registration
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java       # Global error handling
│   │   ├── monitoring/
│   │   │   ├── RequestIdFilter.java              # Request correlation filter
│   │   │   ├── RequestIdManager.java             # Request ID management
│   │   │   └── PerformanceTimer.java             # Performance timing
│   │   ├── dto/
│   │   │   └── ErrorResponse.java                # Standard error DTO
│   │   ├── cache/
│   │   │   ├── CacheConfig.java                  # Cache configuration
│   │   │   ├── CacheProperties.java              # Cache properties
│   │   │   └── IndexerResponseCache.java         # Response cache implementation
│   │   ├── client/
│   │   │   ├── IndexerRestClient.java            # Indexer REST client interface
│   │   │   ├── IndexerRestClientImpl.java        # Indexer REST client implementation
│   │   │   ├── config/
│   │   │   │   ├── IndexerProperties.java        # Indexer connection properties
│   │   │   │   └── RestClientConfig.java         # REST client bean configuration
│   │   │   ├── dto/                              # Request/response DTOs
│   │   │   ├── exception/                        # Indexer exception hierarchy
│   │   │   └── service/                          # Business services
│   │   └── tools/                                # MCP tool implementations
│   │       ├── PingTool.java
│   │       ├── RepositorySummaryTool.java
│   │       ├── RepositoryStatisticsTool.java
│   │       ├── SearchCodeTool.java
│   │       ├── FindSpringComponentTool.java
│   │       ├── FindRestApiTool.java
│   │       ├── FindDependencyTool.java
│   │       ├── FindClassTool.java
│   │       ├── FindMethodTool.java
│   │       ├── ListRelatedFilesTool.java
│   │       ├── BuildContextTool.java
│   │       ├── DevelopmentContextTool.java
│   │       ├── PromptContextTool.java
│   │       ├── AnalyzeTaskTool.java              # Phase 2 tools
│   │       ├── AssembleContextTool.java
│   │       ├── AnalyzeImpactTool.java
│   │       ├── ImplementationPlanTool.java
│   │       ├── TestImpactAnalysisTool.java
│   │       ├── RefactoringAssistantTool.java
│   │       ├── ArchitectureInsightsTool.java
│   │       ├── RepositoryHealthTool.java
│   │       └── RepositoryConventionTool.java
│   └── resources/
│       └── application.yml                       # Application configuration
└── test/java/com/projectiq/mcp/                  # Unit and integration tests
    ├── analysis/                                 # Phase 2 analysis tests
    │   ├── dto/                                  # DTO tests
    │   └── service/                              # Service tests
    ├── cache/                                    # Cache tests
    ├── client/                                   # Client tests
    │   ├── config/
    │   ├── dto/
    │   ├── exception/
    │   └── service/
    ├── dto/
    ├── exception/
    ├── monitoring/
    └── tools/                                    # MCP tool tests
```

## Development

### Building

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Adding a New Tool

1. Create a new `@Component` class in `src/main/java/com/projectiq/mcp/tools/`
2. Add a method annotated with `@Tool(description = "...")`
3. Register the tool in `McpServerConfig.java`
4. Add unit tests in the corresponding test directory
5. For analysis tools, add service and DTO in the `analysis/` package

## Phase 4: AI Development Intelligence

Phase 4 introduces a complete **AI Development Intelligence Platform** that extends Phase 3 with advanced repository analysis capabilities. This phase adds code change analysis, dependency prediction, refactoring simulation, repository evolution analysis, development strategy advisory, repository knowledge graph, cross-repository analysis, architectural decision advisory, and development knowledge engine.

### Phase 4 Architecture

```
Developer Request
         ↓
  [Code Change Analysis] ──→ Change impact prediction
         ↓
  [Dependency Prediction] ──→ Dependency change forecasting
         ↓
  [Refactoring Simulation] ──→ Refactoring impact simulation
         ↓
  [Repository Evolution] ──→ Codebase evolution analysis
         ↓
  [Development Strategy] ──→ Strategic development advisory
         ↓
  [Knowledge Graph] ──→ Repository relationship mapping
         ↓
  [Cross-Repository] ──→ Multi-repository comparison
         ↓
  [Architectural Decision] ──→ Architecture decision advisory
         ↓
  [Knowledge Engine] ──→ Development knowledge queries
         ↓
  AI Coding Agent
```

### Phase 4 Services

| Service | Description |
|---------|-------------|
| `CodeChangeAnalysisService` | Analyzes proposed code changes to predict impact on files, classes, methods, and APIs |
| `DependencyChangePredictionService` | Predicts dependency changes required for proposed modifications |
| `RefactoringImpactSimulationService` | Simulates refactoring impact before implementation |
| `RepositoryEvolutionAnalysisService` | Analyzes repository evolution patterns and codebase maturity |
| `DevelopmentStrategyService` | Provides strategic development recommendations based on repository state |
| `RepositoryKnowledgeGraphService` | Builds and queries repository knowledge graphs |
| `CrossRepositoryAnalysisService` | Compares and analyzes multiple repositories |
| `ArchitecturalDecisionService` | Provides architectural decision based on repository context |
| `DevelopmentKnowledgeService` | Queries development knowledge across multiple domains |

### Phase 4 MCP Tools

| Tool | Description |
|------|-------------|
| `analyze_code_change` | Analyzes proposed code changes and predicts impact |
| `predict_dependency_change` | Predicts dependency changes for proposed modifications |
| `simulate_refactoring` | Simulates refactoring impact before implementation |
| `analyze_repository_evolution` | Analyzes repository evolution and codebase maturity |
| `recommend_development_strategy` | Recommends development strategies based on repository state |
| `query_repository_graph` | Queries repository knowledge graph for relationships |
| `cross_repository_analysis` | Compares and analyzes multiple repositories |
| `architectural_decision` | Provides architectural decision advisory |
| `query_development_knowledge` | Queries development knowledge across domains |

### Complete MCP Tool Catalog (Phase 1-4)

The ProjectIQ MCP Server exposes **44 MCP tools** across all phases:

**Phase 1 - Repository Intelligence (13 tools):**
- `ping`, `repository_summary`, `repository_statistics`, `search_code`
- `find_spring_component`, `find_rest_api`, `find_dependency`, `find_class`, `find_method`
- `list_related_files`, `build_context`, `development_context`, `prompt_context`

**Phase 2 - Intelligent Developer Workflow (9 tools):**
- `analyze_task`, `assemble_context`, `analyze_impact`, `implementation_plan`
- `test_impact_analysis`, `refactoring_assistant`, `architecture_insights`
- `repository_health`, `repository_conventions`

**Phase 3 - AI Agent Orchestration (13 tools):**
- `orchestrate_workflow`, `execute_workflow`, `build_context_pipeline`, `plan_execution`
- `validate_workflow`, `generate_recommendations`, `assess_execution_readiness`
- `create_development_session`, `get_development_session`, `resume_development_session`, `complete_development_session`
- `export_agent_handoff`, `import_agent_handoff`, `execute_end_to_end_workflow`

**Phase 4 - AI Development Intelligence (9 tools):**
- `analyze_code_change`, `predict_dependency_change`, `simulate_refactoring`
- `analyze_repository_evolution`, `recommend_development_strategy`, `query_repository_graph`
- `cross_repository_analysis`, `architectural_decision`, `query_development_knowledge`

### Platform Integration

The complete platform integrates all Phase 1-4 services into a unified deterministic intelligence platform:

```
┌─────────────────────────────────────────────────────────────────┐
│                    ProjectIQ MCP Server                          │
├─────────────────────────────────────────────────────────────────┤
│  Phase 1: Repository Intelligence                               │
│  ├── IndexerRestClient (HTTP communication)                     │
│  ├── RepositoryContextBuilderService                            │
│  └── DevelopmentContextService, PromptContextService            │
├─────────────────────────────────────────────────────────────────┤
│  Phase 2: Intelligent Developer Workflow                        │
│  ├── TaskAnalysisService, ContextAssemblyService                │
│  ├── ImpactAnalysisService, ImplementationPlanningService       │
│  ├── TestImpactAnalysisService, RefactoringAssistantService     │
│  └── ArchitectureInsightsService, RepositoryHealthService       │
├─────────────────────────────────────────────────────────────────┤
│  Phase 3: AI Agent Orchestration                                │
│  ├── WorkflowOrchestratorService, WorkflowExecutionService      │
│  ├── IntelligentContextPipelineService, ExecutionPlanningService│
│  ├── WorkflowValidationService, RecommendationEngineService     │
│  ├── ExecutionReadinessService, DevelopmentSessionService       │
│  └── AgentHandoffService, IntegrationOrchestratorService        │
├─────────────────────────────────────────────────────────────────┤
│  Phase 4: AI Development Intelligence                           │
│  ├── CodeChangeAnalysisService, DependencyChangePredictionService│
│  ├── RefactoringImpactSimulationService                         │
│  ├── RepositoryEvolutionAnalysisService, DevelopmentStrategyService│
│  ├── RepositoryKnowledgeGraphService, CrossRepositoryAnalysisService│
│  └── ArchitecturalDecisionService, DevelopmentKnowledgeService  │
└─────────────────────────────────────────────────────────────────┘
```

### Deterministic Execution

All Phase 4 services maintain full deterministic execution:
- No AI/LLM integration
- No autonomous coding
- No repository modification
- No git integration
- No self-learning
- Stable, repeatable results for identical inputs

## Release Notes

### Phase 4 - Feature 10: Final Integration and Validation

**New Integration Tests:**
- Comprehensive platform integration validation
- MCP tool registration verification
- Performance validation metrics
- End-to-end workflow testing
- Deterministic behavior verification

**Platform Capabilities:**
- 44 MCP tools across 4 phases
- 18 intelligent services
- Complete developer workflow pipeline
- Repository knowledge graph
- Cross-repository analysis
- Architectural decision advisory
- Development knowledge engine

**Quality Metrics:**
- 1588 unit and integration tests
- Full backward compatibility
- Zero breaking changes
- Deterministic execution verified

## License

This project is part of ProjectIQ.
