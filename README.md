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

## Intelligent Developer Workflow

The Phase 2 tools can be chained together to form a complete deterministic workflow. The following sequence is recommended for AI coding agents:

1. **`analyze_task`** — Understand the request (task type, entities, complexity)
2. **`assemble_context`** — Gather relevant repository context
3. **`analyze_impact`** — Evaluate the impact of the proposed change
4. **`implementation_plan`** — Generate a structured implementation plan
5. **`test_impact_analysis`** — Identify affected tests and testing scope
6. **`refactoring_assistant`** — Get refactoring recommendations if needed
7. **`architecture_insights`** — Understand the architecture for informed decisions
8. **`repository_health`** — Assess repository health before making changes
9. **`repository_conventions`** — Discover conventions to maintain consistency

Each tool provides deterministic, structured JSON responses that guide the AI agent through the development process without generating or modifying code.

### Example Workflow: Complete Feature Implementation

1. **Task Analysis**: `analyze_task("Add pagination to UserController", "my-project")`
   - → Returns: task type (NEW_FEATURE), entities, complexity (MEDIUM)
2. **Context Assembly**: `assemble_context("Add pagination to UserController", "my-project")`
   - → Returns: related files, classes, methods, dependencies
3. **Impact Analysis**: `analyze_impact("Add pagination to UserController", "my-project")`
   - → Returns: scope (MODERATE), risk (LOW), components affected
4. **Implementation Plan**: `implementation_plan("Add pagination to UserController", "my-project")`
   - → Returns: ordered steps, files to modify, validation steps
5. **Test Impact**: `test_impact_analysis("Add pagination to UserController", "my-project")`
   - → Returns: affected tests, missing tests, testing effort
6. **Architecture Insights**: `architecture_insights("my-project")`
   - → Returns: layered architecture, dependency flows
7. **Repository Health**: `repository_health("my-project")`
   - → Returns: coverage gaps, code quality issues
8. **Repository Conventions**: `repository_conventions("my-project")`
   - → Returns: naming patterns, code style conventions

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

## License

This project is part of ProjectIQ.