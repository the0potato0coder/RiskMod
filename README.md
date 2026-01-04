# RiskMod

A financial software service that demystifies portfolio analysis through comprehensive risk and performance metrics.

## Overview

RiskMod is a Spring Boot application that provides portfolio management and analysis capabilities. It allows users to create portfolios, track assets, and perform sophisticated financial analysis including volatility calculations, Sharpe ratios, and performance metrics.

## Features

- **User Authentication**: Secure JWT-based authentication and authorization
- **Portfolio Management**: Create and manage multiple investment portfolios
- **Asset Tracking**: Add and monitor individual assets within portfolios
- **Performance Analysis**: Calculate total returns, gains/losses, and portfolio valuation
- **Risk Analysis**: Compute volatility and Sharpe ratios based on historical data
- **Real-time Market Data**: Integration with Alpha Vantage API for live stock quotes and historical prices

## Technologies Used

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **Lombok** for boilerplate reduction
- **Maven** for dependency management
- **Alpha Vantage API** for financial data

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- Alpha Vantage API key (free tier available at https://www.alphavantage.co/)

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/the0potato0coder/RiskMod.git
   cd RiskMod
   ```

2. **Configure MySQL Database**
   
   Create a MySQL database (the application will auto-create it if it doesn't exist):
   ```sql
   CREATE DATABASE riskmod_db;
   ```

3. **Configure Application Properties**
   
   Update `src/main/resources/application.properties` with your settings:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/riskmod_db?createDatabaseIfNotExist=true
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   
   # Alpha Vantage API Key
   alphavantage.api.key=your_api_key_here
   ```

4. **Build the project**
   ```bash
   ./mvnw clean install
   ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`

## API Endpoints

### Authentication

#### Register a new user
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```
Returns a JWT token for authenticated requests.

### Portfolio Management

All portfolio endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

#### Create a portfolio
```http
POST /api/portfolios
Content-Type: application/json

{
  "name": "My Investment Portfolio"
}
```

#### Get all portfolios
```http
GET /api/portfolios
```

#### Add an asset to a portfolio
```http
POST /api/portfolios/{portfolioId}/assets
Content-Type: application/json

{
  "tickerSymbol": "AAPL",
  "quantity": 10,
  "purchasePrice": 150.00
}
```

#### Get all assets in a portfolio
```http
GET /api/portfolios/{portfolioId}/assets
```

#### Get full analysis (performance + risk)
```http
GET /api/portfolios/{portfolioId}/analysis
```

Returns:
- **Performance Metrics**: Total original cost, current value, gain/loss, and return percentage
- **Risk Metrics**: Annualized volatility and Sharpe ratio

## Project Structure

```
src/
├── main/
│   ├── java/com/jugantar/RiskMod/
│   │   ├── config/          # Security and application configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # JPA entity models
│   │   ├── repository/      # Data access layer
│   │   ├── security/        # JWT authentication components
│   │   └── service/         # Business logic layer
│   └── resources/
│       └── application.properties
└── test/                    # Unit and integration tests
```

## Key Components

### Models
- **User**: User accounts with authentication
- **Portfolio**: Investment portfolios owned by users
- **Asset**: Individual stocks/securities within portfolios

### Services
- **AuthService**: User registration and authentication
- **PortfolioService**: Portfolio CRUD operations
- **AssetService**: Asset management within portfolios
- **AnalysisService**: Financial calculations (performance, risk, Sharpe ratio)
- **FinancialDataService**: Integration with Alpha Vantage API

## Security

- All endpoints except `/api/auth/register` and `/api/auth/login` require authentication
- JWT tokens are used for stateless authentication
- Passwords are encrypted using BCrypt
- Portfolio and asset access is restricted to the owning user

## Analysis Metrics

### Performance Analysis
- **Total Original Cost**: Sum of all asset purchase prices
- **Total Current Value**: Current market value based on latest prices
- **Total Gain/Loss**: Difference between current value and original cost
- **Return Percentage**: Percentage return on investment

### Risk Analysis
- **Volatility**: Annualized standard deviation of daily returns (expressed as percentage)
- **Sharpe Ratio**: Risk-adjusted return metric using a 2% annualized risk-free rate

## API Rate Limits

The application uses Alpha Vantage's free tier API, which has the following limits:
- 25 API calls per day
- The application includes a 1-second delay between API calls to respect rate limits

For production use, consider:
- Upgrading to a premium Alpha Vantage plan
- Implementing data caching
- Using alternative financial data providers

## Development

### Building the project
```bash
./mvnw clean package
```

### Running tests
```bash
./mvnw test
```

### Viewing SQL queries
Set `spring.jpa.show-sql=true` in `application.properties` to see all SQL queries in the console.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is currently under development. License information will be added soon.

## Contact

For questions or support, please open an issue in the GitHub repository.

## Acknowledgments

- Alpha Vantage for providing financial market data API
- Spring Boot community for excellent documentation and support
