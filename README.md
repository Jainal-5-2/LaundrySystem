# Laundry Management System

A desktop application for running a laundry shop: taking customer orders, tracking each order from drop-off to pickup, recording payments, printing receipts, and viewing sales reports. Built with **Java Swing** (FlatLaf look and feel) and **MySQL**.

Orders are priced per kilo or per item. Customers can pay in full or in parts, and the system always shows what is still owed.

## Features

### Dashboard
- Today's orders, today's sales, outstanding balance, and orders ready for pickup
- Sales bar chart for the last 7, 14, or 30 days
- The 10 most recent orders, with how long ago each was placed

### Orders
- Split view: order list on the left, full order details on the right
- Filter the list by status (All, Received, Washing, Ready, Claimed)
- **New order:** pick a customer, add one or more services with a quantity (kilos or items, in steps of 0.5), and optionally record a payment right away
- Update the order status from the detail panel
- **Add payment:** record partial or full payments; the amount cannot exceed the balance
- **Print receipt:** shows a text receipt and sends it to a printer

### Customers
- Add, update, and delete customers (name, phone, address)
- A customer who has orders cannot be deleted

### Services & Pricing
- Add, update, and delete services, each priced per kilo (`PER_KILO`) or per item (`PER_ITEM`)
- A service used in existing orders cannot be deleted

### Reports
- Choose a date range, or use the presets: Today, This Week, This Month, Last 30 Days, This Year
- Summary: orders, sales, amount collected, amount unpaid
- Daily breakdown table (sortable)
- Sales by service: number of orders, quantity, and revenue per service

### Accounts
- Create an account and sign in from the login screen
- Passwords are hashed with BCrypt

## Order workflow

```
RECEIVED ──▶ WASHING ──▶ READY ──▶ CLAIMED
```

- New orders start as **Received**.
- The status is changed from a drop-down in the order details.
- An order's **balance** is its total minus everything paid so far, and it is shown in red while unpaid.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java (NetBeans Ant project, JDK 8 or newer) |
| UI | Swing + [FlatLaf](https://www.formdev.com/flatlaf/) |
| Charts | JFreeChart 1.5.3 |
| Date picker | SwingX 1.6.4 (`JXDatePicker`) |
| Password hashing | jBCrypt 0.4 |
| Database | MySQL via MySQL Connector/J |

## Project structure

```
LaundrySystem/
├── schema.sql                  # Database, tables, and sample services
├── lib/                        # Place the library JARs here
├── build.xml, manifest.mf, nbproject/   # NetBeans project files
└── src/laundry/
    ├── Main.java               # Entry point: global look and feel, opens login
    ├── LoginFrame.java         # Sign-in and account creation
    ├── MainFrame.java          # Main window with tabs
    ├── DashboardPanel.java     # Dashboard tab
    ├── OrderPanel.java         # Orders tab (list, details, new order, receipt)
    ├── CustomerPanel.java      # Customers tab
    ├── ServicePanel.java       # Services & Pricing tab
    ├── ReportPanel.java        # Reports tab
    ├── DB.java                 # Connection settings and query helpers
    ├── OrderDAO.java           # Order, item, and payment queries
    ├── ReportDAO.java          # Dashboard and report queries
    ├── UserDAO.java            # Login and registration
    └── ...                     # Theme, Fmt, BarChart, and small UI components
```

## Requirements

- JDK 8 or newer
- NetBeans (the project is a NetBeans Ant project)
- MySQL Server 8+
- These library JARs:
  - FlatLaf
  - MySQL Connector/J
  - jBCrypt 0.4
  - SwingX (`swingx-all-1.6.4`)
  - JFreeChart 1.5.3

## Setup

### 1. Create the database

```bash
mysql -u root -p < schema.sql
```

This creates the `laundry_db` database, all tables, and four sample services.

> Run it once. The script uses plain `CREATE TABLE`, so running it again on the same database fails with "table already exists". To start over, drop the `laundry_db` database first.

### 2. Configure the connection

Open `src/laundry/DB.java` and set your MySQL credentials:

```java
private static final String USER = "root";
private static final String PASS = "root";   // change to your password
```

### 3. Add the libraries

Open the project in NetBeans, then right-click the project > **Properties** > **Libraries** > **Classpath**, remove any broken entries, and add all five JARs listed under Requirements (**Add JAR/Folder**).

The project settings contain paths that only exist on the original developer's computer, so the libraries must be re-added on a new machine.

### 4. Run

Press **Run** (F6). The main class is `laundry.Main`.

## First use

The database has **no user accounts**, so create one first:

1. On the login screen, click **Create account**.
2. Enter a username and a password of at least 6 characters.
3. Sign in with it.

Then add a few customers in the **Customers** tab, and you can start creating orders. The four sample services are:

| Service | Pricing | Price (PHP) |
|---|---|---|
| Wash & Fold | per kilo | 50.00 |
| Wash, Dry & Fold | per kilo | 70.00 |
| Dry Cleaning | per item | 150.00 |
| Comforter / Blanket | per item | 200.00 |

## Database design

| Table | Purpose |
|---|---|
| `customers` | Customer name, phone, address |
| `services` | Service name, pricing type (`PER_KILO` / `PER_ITEM`), price |
| `orders` | One row per order: customer, status, total, amount paid |
| `order_items` | Services in an order, with quantity, unit price, and subtotal |
| `payments` | Every payment made toward an order |
| `users` | Login accounts (username and BCrypt password hash) |

New orders and payments are saved as database transactions, so an order, its items, and its first payment are saved together or not at all.

## Notes and limitations

- All accounts have the same access. There are no separate admin and staff roles.
- Database credentials are hard-coded in `DB.java`.
- Prices are copied into each order item when the order is created, so changing a service's price later does not change existing orders.
- Amounts are shown in Philippine pesos (PHP).
- This is a school project, not a production system. It has no order editing or cancellation, and no SMS or email notifications.