# TedTalks Influence Analysis API

**iO Knowledge Sharing Platform: TedTalks Edition**

## Overview
This Spring Boot application provides:

- **CSV Import** of TedTalks data
- **CRUD** operations for Speakers and Talks
- **Influence Analysis** of speakers (total and per year)

It fulfills the technical assessment by iO Digital to manage data and surface insights on speaker influence.

## Features

1. **Data Import**
    - Upload a CSV file via `POST /api/import/talks`
    - Robust error handling with detailed `ImportError` entries
2. **Data Management (CRUD)**
    - **Speakers**: `GET /api/speakers`, `GET /api/speakers/{id}`, `GET /api/speakers/{name}`, `POST`, `PUT`, `DELETE`
    - **Talks**: `GET /api/talks`, `GET /api/talks/{id}`, `POST`, `PUT`, `DELETE`
3. **Influence Analysis**
    - **List influence**: `GET /api/speakers/influence` (optional `?year=YYYY`)
    - **Top speaker**: `GET /api/speakers/influence/most-influential?year=YYYY`
    - Configurable via Strategy Pattern (`InfluenceStrategy`)

## Tech Stack

- **Java 17**
- **Spring Boot** (Web, JDBC)
- **Lombok** for boilerplate reduction
- **H2** in-memory database
- **OpenCSV** for CSV parsing
- **SpringDoc OpenAPI** (Swagger UI)

## Project Structure

```
src/main/java/com/narciso/tedtalks
├── common
│   ├── errors
│   │   ├── ErrorType.java
│   │   └── ImportError.java
│   ├── exception
│   │   └── ResourceNotFoundException.java
│   └── utils
│       └── SortUtils.java
├── config
│   └── OpenApiConfig.java
├── imports
│   ├── controller
│   │   └── ImportController.java
│   ├── domain
│   │   └── ImportResult.java
│   ├── dto
│   │   └── CsvRecord.java
│   └── service
│       └── ImportService.java
├── speakers
│   ├── controller
│   │   ├── SpeakerController.java
│   │   └── InfluenceController.java
│   ├── dao
│   │   ├── SpeakerDao.java
│   │   └── SpeakerRowMapper.java
│   ├── domain
│   │   ├── Speaker.java
│   │   └── MostInfluentialSpeaker.java
│   ├── dto
│   │   └── SpeakerInfluenceDto.java
│   ├── service
│   │   ├── SpeakerService.java
│   │   └── InfluenceService.java
│   └── strategy
│       ├── InfluenceStrategy.java
│   │   ├── CompositeStrategy.java
│   │   └── WeightedStrategy.java
├── talks
│   ├── controller
│   │   └── TalkController.java
│   ├── dao
│   │   ├── TalkDao.java
│   │   └── TalkRowMapper.java
│   ├── domain
│   │   └── Talk.java
│   └── service
│       └── TalkService.java
└── TedtalksApplication.java

src/main/resources
├── static/
├── templates/
│   ├── iO_Data.csv
│   └── TedTalks.postman_collection.json
├── application.properties
└── schema.sql

src/test/java/com/narciso/tedtalks
├── ImportServiceTest.java
├── InfluenceServiceTest.java
├── SpeakerServiceTest.java
├── TalkServiceTest.java
└── TedtalksApplicationTests.java
```

## Setup & Run

### Prerequisites

- Java 17
- Maven 3.x

### Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd tedtalks
   ```
2. **Build & Test**
   ```bash
   mvn clean install
   ```
3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   or
   ```bash
   java -jar target/tedtalks-0.0.1-SNAPSHOT.jar
   ```
4. **H2 Console** (optional)
    - URL: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:tedtalksdb`
    - User: `sa`, Password: `<empty>`

The database schema is auto-created from `schema.sql` on startup.

## Configuration

All settings are in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:tedtalksdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## API Documentation

Swagger UI is available at:  
`http://localhost:8080/swagger-ui.html`

## Usage Examples

### Variables

```bash
# Base URL
{{base_url}} = http://localhost:8080
# Speaker ID
{{speaker_id}} = 1
# Talk ID
{{talk_id}} = 1
# Speaker Name
{{speaker_name}} = "John Doe"
# Updated Speaker Name
{{updated_speaker_name}} = "Jane Doe"
# Talk Title
{{talk_title}} = "The Future of Tech"
# Talk Date
{{talk_date}} = "2023-10-01"
# Talk Views
{{talk_views}} = 1000
# Talk Likes
{{talk_likes}} = 100
# Talk Link
{{talk_link}} = "http://example.com/talk"
# Talk Speaker ID
{{talk_speaker_id}} = 1
# Updated Talk Title
{{updated_talk_title}} = "The Future of AI"
# Updated Talk Views
{{updated_talk_views}} = 1500
# Updated Talk Likes
{{updated_talk_likes}} = 200
# Year
{{year}} = 2023
```

### Import CSV

```bash
# Import Talks CSV
curl -X POST http://{{base_url}}/api/import/talks \
  -H "Content-Type: multipart/form-data" \
  -F "file=@iO_Data.csv"
```

### CRUD Speakers

```bash
# Get All Speakers
curl http://{{base_url}}/api/speakers

# Get Speaker by ID
curl http://{{base_url}}/api/speakers/{{speaker_id}}

# Analyze Speaker Influence (Total or by Year)
curl http://{{base_url}}/api/speakers/{{speaker_id}}/influence?year={{year}}

# Get Most Influential Speaker of the Year
curl http://{{base_url}}/api/speakers/influence/most-influential?year={{year}}

# Create Speaker
curl -X POST http://{{base_url}}/api/speakers \
  -H "Content-Type: application/json" \
  -d '{"name":"{{speaker_name}}"}'

# Update Speaker
curl -X PUT http://{{base_url}}/api/speakers/{{speaker_id}} \
  -H "Content-Type: application/json" \
  -d '{"name":"{{updated_speaker_name}}"}'

# Delete Speaker
curl -X DELETE http://{{base_url}}/api/speakers/{{speaker_id}}
```

### CRUD Talks

```bash
# Get All Talks
curl http://{{base_url}}/api/talks

# Get Talk by ID
curl http://{{base_url}}/api/talks/{{talk_id}}

# Create Talk
curl -X POST http://{{base_url}}/api/talks \
  -H "Content-Type: application/json" \
  -d '{
    "title":"{{talk_title}}",
    "date":"{{talk_date}}",
    "views":{{talk_views}},
    "likes":{{talk_likes}},
    "link":"{{talk_link}}",
    "speakerId":{{talk_speaker_id}}
}'

# Update Talk
curl -X PUT http://{{base_url}}/api/talks/{{talk_id}} \
  -H "Content-Type: application/json" \
  -d '{
    "title":"{{updated_talk_title}}",
    "date":"{{talk_date}}",
    "views":{{updated_talk_views}},
    "likes":{{updated_talk_likes}},
    "link":"{{talk_link}}",
    "speakerId":{{talk_speaker_id}}
}'

# Delete Talk
curl -X DELETE http://{{base_url}}/api/talks/{{talk_id}}
```

### Influence Analysis

```bash
# All years
curl http://localhost:8080/api/speakers/influence

# Specific year
curl http://localhost:8080/api/speakers/influence?year=2021

# Top speaker in year
curl http://localhost:8080/api/speakers/influence/most-influential?year=2021
```

## Design Decisions & Assumptions

- **H2 In-Memory**: Using an in-memory database eliminates external dependencies and configuration overhead, allowing evaluators to run the application immediately without installing or connecting to a separate database. The schema is auto-applied on startup, providing a fresh, consistent state for each run and simplifying cleanup.

- **JDBC Template**: By leveraging Spring’s JdbcTemplate, we maintain explicit control over SQL queries and performance characteristics, facilitating easier debugging and optimization. This choice avoids ORM complexity and hidden behavior, making data access straightforward and transparent.

- **Date Parsing**: Parsing dates as YearMonth with the fixed format `MMMM yyyy` standardizes input interpretation and locale (EN), ensuring consistent conversion of month-year strings (e.g., "January 2020") and preventing ambiguity in date handling.

- **Duplicate Prevention**: Enforcing uniqueness based on a composite key (`title + author + date`) prevents redundant or conflicting talk entries. This strategy maintains data integrity, avoids duplicate records, and simplifies downstream calculations and analyses.

- **Transactional Boundaries**: Wrapping each CSV record import in its own transaction isolates failures so that one bad record does not roll back successful imports. Errors are collected as ImportError entries, enabling comprehensive reporting while preserving valid data.

- **Strategy Pattern for Influence Calculation**: The InfluenceStrategy interface provides flexibility to swap or extend scoring algorithms. The default **WeightedStrategy** (70% views, 30% likes) offers a simple yet balanced metric, and new strategies (e.g., time-decay, engagement-weighted) can be added without modifying existing code.

## Evaluation Criteria Addressed

- **Code Quality**: The codebase follows a layered, feature-first package structure, applies Lombok to reduce boilerplate, and uses clear naming conventions. This organization improves readability, maintainability, and onboarding speed for new contributors.

- **Problem Solving**: The CSV import pipeline is resilient, with detailed error reporting and isolation of faulty records. Edge cases such as missing fields, invalid formats, and constraint violations are captured and reported without halting the entire import.

- **Required Features**: All assessment requirements—CSV import, CRUD endpoints for speakers and talks, and influence analysis (total and per year)—are implemented and covered by unit and integration tests.

- **Documentation**: The HELP.md provides comprehensive setup instructions, usage examples, and API details. Inline code comments clarify complex logic, and Swagger UI (via SpringDoc) offers interactive exploration of endpoints.

---

*Developed by Rodolpho Narciso — iO Digital Technical Assessment*

