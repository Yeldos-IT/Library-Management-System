# 📚 Library Management System

A **modern and secure JavaFX-based desktop application** for managing a library system, integrated with **MySQL**. The project features role-based access, allowing administrators and users to manage books efficiently and securely.

---

![Library Animation](https://media.giphy.com/media/3o7abldj0b3rxrZUxW/giphy.gif)

---

## 🚀 Features

### **Authentication**
🔒 Secure login system with role-based access:
- Admin Panel
- User Panel

### **Admin Panel**
- Add, update, and delete books.
- Export the book list to a CSV file.
- Filter and search books by:
  - Author
  - Year
  - Title keywords.
- Open associated book PDFs.

### **User Panel**
- Browse and view available books.
- Filter and search books by:
  - Author
  - Year
  - Title keywords.
- Open associated book PDFs.

---

## 🎨 Project Structure

```
Library/
│
├── src/
│   ├── application/
│   │   └── Main.java                # Main entry point of the application
│   ├── controller/
│   │   ├── AdminController.java    # Handles admin-related functionalities
│   │   ├── AuthController.java     # Handles user authentication
│   │   └── UserController.java     # Handles user-related functionalities
│   ├── library/
│   │   ├── Books.java              # Represents book data structure
│   │   └── User.java               # Represents user data structure
│   └── view/
│       ├── AdminPane.fxml          # Admin panel UI
│       ├── AuthPane.fxml           # Authentication UI
│       └── UserPane.fxml           # User panel UI
│
├── lib/
│   └── mysql-connector-j-9.1.0.jar # MySQL Connector library
│
├── resources/
│   ├── styles.css                  # CSS for styling JavaFX application
│   ├── icons/                      # Icons used in the UI
│   └── pdfs/                       # Sample PDF files for books
│
└── module-info.java                # Module dependencies for JavaFX and MySQL
```

---

## 🛠 Technologies Used

### **Java and JavaFX**
- **JDK Version:** OpenJDK 23.
  - Supports modern Java features like primitive types in patterns and implicitly declared classes.
- **JavaFX Version:** 23.0.1.
  - Modules included: `javafx.controls`, `javafx.fxml`, `javafx.graphics`.

### **Database Integration**
- **MySQL Server:**
  - Used for storing book and user data.
  - Integration via `mysql-connector-j-9.1.0.jar`.

### **Security Features**
- **Password Hashing:** MD5 hashing for securely storing user passwords.

---

## 🛡️ Key Functionalities

### **Validation**
- Ensures all required fields are filled during book addition or update.
- Validates data formats, such as numeric fields for year and pages.

### **Filtering and Sorting**
- Filter books by:
  - Author
  - Year
  - Title keywords.
- Sort data in the `TableView` by any column.

### **Data Export**
- Export book data to a CSV file for analysis or backup.

### **File Management**
- Open book PDFs directly from the application.

---

![CSV Export Example](https://via.placeholder.com/800x400?text=CSV+Export+Demo)

![Filtering Example](https://via.placeholder.com/800x400?text=Filtering+Books)

---

## ⚙️ Setup and Execution

### **Pre-requisites**
1. **Java Development Kit (JDK):** Version 23.
2. **MySQL Server:** Ensure the database is running, and the schema is set up correctly.
3. **JavaFX SDK:** Version 23.0.1 or later.
4. **MySQL Connector:** Already included in the `lib/` folder.

### **Steps to Run**
1. **Import the Project**:
   - Import into your IDE (e.g., IntelliJ IDEA).
2. **Configure Database**:
   - Update the database credentials in `connectToDatabase` methods in controllers.
3. **Run the Application**:
   - Build and execute `Main.java` to start the application.
4. **Login to the System**:
   - Use credentials stored in the MySQL database.

---

## 📋 Database Schema

### **Books Table**
| Column  | Type        | Description         |
|---------|-------------|---------------------|
| `id`    | INT (PK)    | Unique book ID      |
| `title` | VARCHAR(255)| Title of the book   |
| `author`| VARCHAR(255)| Author's name       |
| `year`  | INT         | Year of publication |
| `pages` | INT         | Number of pages     |
| `pdfPath`| VARCHAR(255)| Path to the book PDF|

### **Users Table**
| Column    | Type        | Description         |
|-----------|-------------|---------------------|
| `id`      | INT (PK)    | Unique user ID      |
| `username`| VARCHAR(255)| Username            |
| `password`| VARCHAR(255)| Password (hashed)   |
| `role`    | VARCHAR(50) | User role (e.g., admin, user) |

---

![User Interface Example](https://via.placeholder.com/800x400?text=Library+Management+UI)

---

## 🌟 How to Extend
- **Pagination:** Add pagination to `TableView` for better handling of large datasets.
- **Enhanced User Roles:** Introduce additional roles like librarians with specific permissions.
- **Cloud Storage:** Use cloud storage for book PDFs and link URLs in the database.

---

## ⚠️ Known Issues
- Ensure valid PDF paths; incorrect paths will cause errors when opening files.
- Database credentials are hardcoded; consider moving them to a configuration file for better security.

---

## 📜 License
This project is for educational purposes only.
