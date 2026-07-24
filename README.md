# ProjectIQ MCP Server

A standalone MCP (Model Context Protocol) server that exposes ProjectIQ Indexer capabilities as MCP tools for AI coding agents like Cline.

## Overview

ProjectIQ MCP Server bridges the gap between **ProjectIQ Indexer** (a code intelligence engine) and **AI coding agents** (like Cline) by implementing the Model Context Protocol (MCP). This allows AI agents to query repository structure, search code, find Spring components, inspect dependencies, and build rich development context — all through standardised MCP tool calls.

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
- **ProjectIQ MCP Server**: Spring Boot application exposing repository intelligence as MCP tools
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
    com.projectiq.mcp: DEBUG             # Detailed MCP tool logging
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

## Project Structure

```
src/
├── main/
│   ├── java/com/projectiq/mcp/
│   │   ├── ProjectIqMcpServerApplication.java    # Application entry point
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
│   │       └── PromptContextTool.java
│   └── resources/
│       └── application.yml                       # Application configuration
└── test/java/com/projectiq/mcp/                  # Unit and integration tests
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

## License

This project is part of ProjectIQ.