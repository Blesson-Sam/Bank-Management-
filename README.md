# Bank-Management

## Postman Endpoints: https://blessonsam.postman.co/workspace/Blesson-Sam's-Workspace~71761ab4-d6bd-4e0c-8bdf-dccfc5f03aa5/collection/46342078-bffc3b48-c938-44d9-b62f-be1a834d2cff?action=share&creator=46342078 

## Project Structure 

MinibankapplicationApplication.java  → Main application class  

controller/  
  → CustomerController.java        (Customer REST APIs)  
  → AccountController.java         (Account management APIs)  
  → TransactionController.java     (Transaction APIs)  

service/  
  → CustomerService.java           (Customer business logic)  
  → AccountService.java            (Account & interest logic)  
  → TransactionService.java        (Transaction processing)  

repository/  
  → CustomerRepository.java        (Customer data access)  
  → AccountRepository.java         (Account data access)  
  → TransactionRepository.java     (Transaction data access)  

entity/  
  → Customer.java                  (Customer entity)  
  → Account.java                   (Account entity)  
  → Transaction.java               (Transaction entity)  

dto/  
  → CustomerDto.java               (Customer response DTO)  
  → CreateCustomerRequest.java     (Customer creation request)  
  → AccountDto.java                 (Account response DTO)  
  → CreateAccountRequest.java       (Account creation request)  
  → TransactionDto.java            (Transaction response DTO)  
  → TransferRequest.java           (Money transfer request)  
  → DepositWithdrawRequest.java    (Deposit/withdrawal request)  
  → DtoMapperImp                   (Implements methods from DtoMapper interface)  

exception/  
  → GlobalExceptionHandler.java    (Global error handling)  
  → ApiError.java                  (Error response structure)  
  → ResourceNotFoundException.java (404 errors)  
  → InsufficientFundsException.java (Insufficient funds)  
  → AccountStatusException.java    (Account status errors)  
  → DuplicateResourceException.java (Duplicate resource errors)  
  → ActiveAccountsExistException.java (Already Exist account errors)  

mapper/  
  → DtoMapper.java                 (MapStruct mapping interface)  


## DB Schema

1. Customer Table

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    national_id VARCHAR(20) NOT NULL UNIQUE,
    status customer_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
);


2. Account Table

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type account_type NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5,2) NOT NULL,
    accrued_interest DECIMAL(15,2) DEFAULT 0.00,
    last_interest_calculated TIMESTAMP WITH TIME ZONE,
    status account_status NOT NULL DEFAULT 'ACTIVE',
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
);


3. Transaction Table

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'INTEREST_CREDIT')),
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    description VARCHAR(200),
    from_account_id BIGINT,
    to_account_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_transaction_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);


<img width="468" height="646" alt="image" src="https://github.com/user-attachments/assets/cb51124f-2d23-44f8-9558-a4728e113e6b" />

<img width="451" height="618" alt="image" src="https://github.com/user-attachments/assets/75b16490-e718-462e-a761-1c98ac5f3bde" />
