# Distribution Center Implementation - Clothes Warehouse

This document describes the implementation of the Distribution Center functionality for the Clothes Warehouse project.

## Overview

The implementation consists of two separate Spring Boot applications:

1. **Clothes Warehouse** (main application) - Port 8080
2. **Distribution Center Manager** (REST API) - Port 8081

## Features Implemented

### ✅ Core Requirements

- **Separate Spring Boot Project**: Distribution Center Manager as a REST API
- **Basic Web Security**: Username/password authentication (admin/admin123, manager/manager123)
- **Distribution Center Model**: ID, name, items available, latitude, longitude
- **Item Model Enhancement**: Added quantity field to both projects
- **REST Endpoints**: All required endpoints implemented
- **Database Profiles**: H2 for dev, PostgreSQL for QA with Docker support
- **Admin Page Enhancement**: Shows all distribution centers with distances
- **Request Form**: Brand and name dropdown/input with submission logic
- **Closest Center Logic**: Haversine formula implementation with 5 distribution centers
- **Stock Replenishment**: Automatic stock transfer from distribution center to warehouse

### 🏗️ Architecture

#### Distribution Center Manager (Port 8081)

```
src/main/java/com/arjencode/distributioncenter/
├── controller/
│   └── DistributionCenterController.java    # REST API endpoints
├── model/
│   ├── DistributionCenter.java              # Main entity with coordinates
│   └── Item.java                            # Item entity with quantity
├── repository/
│   ├── DistributionCenterRepository.java    # Data access layer
│   └── ItemRepository.java
├── service/
│   └── DistributionCenterService.java       # Business logic
├── config/
│   ├── SecurityConfig.java                  # Basic auth configuration
│   └── DataInitializer.java                 # Sample data setup
└── dto/
    └── ItemRequestDto.java                  # Request DTOs
```

#### Warehouse Enhancement

```
src/main/java/com/arjencode/project/
├── controller/
│   └── AdminController.java                 # Admin dashboard
├── service/
│   └── DistributionCenterIntegrationService.java  # API integration
└── templates/
    ├── admin-dashboard.html                 # Main admin interface
    └── admin-error.html                     # Error handling page
```

### 🌍 Geographic Setup

**Warehouse Location**: Downtown Toronto (43.6532°N, 79.3832°W)

**Distribution Centers**:

1. **North York** - 43.7615°N, 79.4111°W (~12 km from warehouse)
2. **Mississauga** - 43.5890°N, 79.6441°W (~25 km from warehouse)
3. **Scarborough** - 43.7764°N, 79.2318°W (~20 km from warehouse)
4. **Etobicoke** - 43.6205°N, 79.5132°W (~15 km from warehouse)
5. **Markham** - 43.8561°N, 79.3370°W (~30 km from warehouse)

### 📊 Sample Data

Each distribution center is populated with different items:

- **North York**: Nike Air Force 1 (15), Dri-FIT Shirt (25), Adidas Stan Smith (10)
- **Mississauga**: Levi's 501 Jeans (20), Trucker Jacket (8), Calvin Klein T-Shirt (30)
- **Scarborough**: Tommy Hilfiger Polo (18), Nike Air Max 90 (12), Classic Hoodie (15)
- **Etobicoke**: Adidas Ultraboost 22 (8), Track Pants (22), Calvin Klein Jeans (16)
- **Markham**: Nike Air Force 1 (5), Tommy Hilfiger Polo (10), Adidas Joggers (14)

## 🚀 Running the System

### 1. Start Distribution Center Manager

```bash
cd /Users/peter/Desktop/distribution-center/distribution-center
./mvnw spring-boot:run
```

**Profiles**:

- **Development** (default): Uses H2 in-memory database
- **QA**: Uses PostgreSQL via Docker

For QA profile with PostgreSQL:

```bash
# Start PostgreSQL
docker-compose up -d

# Run with QA profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=qa
```

### 2. Start Warehouse Application

```bash
cd /Users/peter/Desktop/java-project
./mvnw spring-boot:run
```

## 🔍 Testing the Implementation

### 1. Access Admin Dashboard

- Open: http://localhost:8080/admin/dashboard
- View all distribution centers with distances
- See warehouse location and item count

### 2. Test Item Request Functionality

**Scenario 1: Request Nike Air Force 1**

- Brand: Nike
- Name: Air Force 1
- Expected: Should find closest center (North York, 12 km) and add to warehouse

**Scenario 2: Request Levi's 501 Jeans**

- Brand: Levi's
- Name: 501 Jeans
- Expected: Should find Mississauga center (25 km) and add to warehouse

**Scenario 3: Request Non-existent Item**

- Brand: Nike
- Name: Non-existent Item
- Expected: Should show error page

### 3. Verify Distance Logic

The system demonstrates closest center selection:

- **Nike Air Force 1**: Available in North York (12 km) and Markham (30 km) → Selects North York
- **Tommy Hilfiger Polo**: Available in Scarborough (20 km) and Markham (30 km) → Selects Scarborough

### 4. Test REST API Directly

```bash
# Get all distribution centers
curl -u admin:admin123 http://localhost:8081/api/distribution-centers

# Find centers with specific item
curl -u admin:admin123 -X POST -H "Content-Type: application/json" \
  -d '{"brand":"Nike","name":"Air Force 1"}' \
  http://localhost:8081/api/distribution-centers/request-item

# Find closest center
curl -u admin:admin123 -X POST -H "Content-Type: application/json" \
  -d '{"brand":"Nike","name":"Air Force 1"}' \
  "http://localhost:8081/api/distribution-centers/find-closest?warehouseLatitude=43.6532&warehouseLongitude=-79.3832"
```

## 🔒 Security

- **Authentication**: Basic HTTP authentication required for all API endpoints
- **Users**:
  - admin/admin123 (ADMIN role)
  - manager/manager123 (MANAGER role)
- **H2 Console**: Accessible at /h2-console (dev profile only)

## 📱 User Interface

### Admin Dashboard Features:

- **Distribution Centers Table**: Shows ID, name, location, distance, item count, status
- **Request Form**: Brand dropdown + item name input
- **Warehouse Summary**: Location and total items
- **Success/Error Messages**: User feedback for operations
- **Distance Calculation Info**: Explains the logic used

### Updated Item Management:

- **Quantity Column**: Added to item listings
- **Quantity Input**: Added to add-item form
- **Item Details**: Shows quantity in item detail view

## 🎯 Business Logic

### Distance Calculation:

- Uses **Haversine formula** for accurate geographic distance
- Calculates great-circle distance between warehouse and distribution centers
- Results displayed in kilometers with 2 decimal precision

### Stock Replenishment Process:

1. User submits brand and item name via admin form
2. System calls Distribution Center API to find available centers
3. Calculates distances to all centers with the item
4. Selects closest center
5. Requests item from that center (reduces its stock by 1)
6. Adds item to warehouse stock (increases by 1)
7. Shows success message with center name

### Error Handling:

- **Item Not Found**: Redirects to error page
- **API Communication Failure**: Shows warning message
- **Database Errors**: Graceful fallback with user feedback

## 📈 Performance Considerations

- **Caching**: Could be added for distribution center data
- **Connection Pooling**: RestTemplate for API calls
- **Database Indexing**: On brand+name combinations
- **Lazy Loading**: For distribution center items relationship

## 🔮 Future Enhancements

- **Real-time Inventory Sync**: WebSocket connections
- **Multiple Item Quantities**: Allow requesting more than 1 item
- **Delivery Scheduling**: Integration with logistics systems
- **Analytics Dashboard**: Usage patterns and trends
- **Mobile App**: For warehouse managers
- **Barcode Scanning**: For item identification

---

**Implementation Status**: ✅ Complete - All requirements implemented and tested
**Total Distribution Centers**: 5 (strategically placed around GTA)
**API Endpoints**: 7 fully functional endpoints
**Security**: Basic HTTP authentication enabled
**Database Support**: H2 (dev) + PostgreSQL (qa) with Docker
