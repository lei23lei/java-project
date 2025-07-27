# Clothes Warehouse Management System

A Spring Boot application for managing a clothes warehouse with database capabilities, featuring JPA repositories, pagination, sorting, and custom filtering.

## Features

### Database Capabilities ✅

- **JPA Repository with Pagination and Sorting (2%)**: Implemented `ItemRepository` extending `JpaRepository` with comprehensive pagination and sorting capabilities
- **JPA Model Annotations (2%)**: `Item` entity properly annotated with `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, and validation annotations
- **Command Line Runner (1%)**: `DataInitializer` populates the database with sample clothes items on application startup
- **Database Save on Addition (1%)**: Items are automatically saved to PostgreSQL database when added through the form
- **Redirect to List Page (2%)**: After adding an item, users are redirected to the comprehensive list page
- **Bootstrap Styling (1%)**: Modern, responsive UI using Bootstrap 5 with Font Awesome icons
- **Custom Query with Filter Button (1%)**: Custom query to filter items by brand and year 2022, with interactive filter button

### Technical Implementation

#### Database Schema

- **Table**: `items`
- **Fields**: id, name, brand, category, price, year, created_at, updated_at
- **Database**: PostgreSQL

#### Key Components

1. **Item Entity** (`src/main/java/com/arjencode/project/model/Item.java`)

   - JPA annotations for database mapping
   - Validation constraints
   - Automatic timestamp management

2. **ItemRepository** (`src/main/java/com/arjencode/project/repository/ItemRepository.java`)

   - Extends JpaRepository for CRUD operations
   - Custom query methods for filtering
   - Pagination and sorting support

3. **ItemService** (`src/main/java/com/arjencode/project/service/ItemService.java`)

   - Business logic layer
   - Pagination and sorting implementation
   - Custom filtering methods

4. **ItemController** (`src/main/java/com/arjencode/project/controller/ItemController.java`)

   - RESTful endpoints for item management
   - Form handling and validation
   - Pagination and filtering support

5. **DataInitializer** (`src/main/java/com/arjencode/project/config/DataInitializer.java`)

   - CommandLineRunner implementation
   - Sample data population
   - Prevents duplicate data insertion

6. **Thymeleaf Templates**
   - `add-item.html`: Form for adding new items
   - `list-items.html`: Comprehensive list with pagination, sorting, and filtering
   - `item-details.html`: Detailed view of individual items

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

### Database Setup

1. Install PostgreSQL if not already installed
2. Create a new database:
   ```sql
   CREATE DATABASE clothes_warehouse;
   ```
3. Create a user (optional, you can use the default postgres user):
   ```sql
   CREATE USER warehouse_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE clothes_warehouse TO warehouse_user;
   ```

### Application Configuration

1. Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/clothes_warehouse
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Running the Application

1. Clone or download the project
2. Navigate to the project directory
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```
4. Open your browser and navigate to `http://localhost:8080`

## Usage

### Adding Items

1. Click "Add New Item" button
2. Fill in the form with item details
3. Click "Save Item"
4. You'll be redirected to the list page

### Viewing Items

- The main page shows all items with pagination
- Use the sorting dropdown to sort by different fields
- Click the sort direction button to toggle ascending/descending

### Filtering Items

- **Custom Brand + Year 2022 Filter**: Enter a brand name and click the search button
- **Quick Brand Filters**: Click on popular brand buttons
- **Year Filters**: Click on year buttons (2021, 2022, 2023)
- **Clear Filters**: Click "Show All" to remove filters

### Item Details

- Click the eye icon next to any item to view detailed information
- From the details page, you can navigate to view items of the same brand

## API Endpoints

- `GET /` - Redirects to items list
- `GET /items/list` - Display all items with pagination and sorting
- `GET /items/add` - Show add item form
- `POST /items/add` - Save new item
- `GET /items/{id}` - Show item details
- `GET /items/brand/{brand}` - Filter items by brand
- `GET /items/year/{year}` - Filter items by year
- `GET /items/filter?brand={brand}` - Custom filter for brand and year 2022

## Sample Data

The application comes pre-populated with sample clothes items from various brands:

- Nike, Adidas, Levi's, Calvin Klein, Tommy Hilfiger
- Categories: Shoes, Shirts, Pants, Shorts, Jackets, Sweaters, Hoodies, Dresses, Blazers, Underwear
- Years: 2021, 2022, 2023
- Price range: $24.99 - $199.99

## Technologies Used

- **Backend**: Spring Boot 3.5.4, Spring Data JPA, Spring Web
- **Database**: PostgreSQL
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome
- **Build Tool**: Maven
- **Java Version**: 17

## Project Structure

```
src/
├── main/
│   ├── java/com/arjencode/project/
│   │   ├── config/
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   └── ItemController.java
│   │   ├── model/
│   │   │   └── Item.java
│   │   ├── repository/
│   │   │   └── ItemRepository.java
│   │   ├── service/
│   │   │   └── ItemService.java
│   │   └── ProjectApplication.java
│   └── resources/
│       ├── templates/
│       │   ├── add-item.html
│       │   ├── item-details.html
│       │   └── list-items.html
│       └── application.properties
└── test/
    └── java/com/arjencode/project/
        └── ProjectApplicationTests.java
```

## Custom Query Implementation

The custom query for filtering items by brand and year 2022 is implemented in `ItemRepository`:

```java
@Query("SELECT i FROM Item i WHERE i.brand = :brand AND i.year = 2022 ORDER BY i.name ASC")
List<Item> findItemsByBrandAndYear2022(@Param("brand") String brand);
```

This query is accessible through the filter button on the list page and returns items sorted alphabetically by name.
