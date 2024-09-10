def checkdb(cursor):
    # cursor.execute("DROP DATABASE IF EXISTS rmkdb")
    cursor.execute("CREATE DATABASE IF NOT EXISTS rmkdb")
    cursor.execute("USE rmkdb")
    
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS users (
        name VARCHAR(255),
        email VARCHAR(255),
        type ENUM('beneficiary', 'manager', 'organizer'),
        phone BIGINT,
        password VARCHAR(255),
        dob DATE,
        aadharno BIGINT,
        bankaccno BIGINT,
        bank VARCHAR(255),
        profile_pic VARCHAR(255),
        PRIMARY KEY (email, type),
        UNIQUE (aadharno),
        UNIQUE (bankaccno)
    )
    """)
    
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS user_business (
        name VARCHAR(255),
        address VARCHAR(255),
        type VARCHAR(255),
        monthly_revenue BIGINT,
        annual_revenue BIGINT,
        monthly_expense BIGINT,
        annual_expense BIGINT,
        profit_margin INT,
        PRIMARY KEY (name)
    )
    """)
    
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS user_income_savings (
        email VARCHAR(255),
        monthly_income BIGINT,
        annual_income BIGINT,
        sources VARCHAR(255),
        savings_balance BIGINT,
        savings_acc_details VARCHAR(255),
        monthly_savings_contributions BIGINT,
        PRIMARY KEY (email),
        FOREIGN KEY (email) REFERENCES users(email)
    )""")
    
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS feedback (
        email VARCHAR(255),
        feedback_text VARCHAR(255),
        PRIMARY KEY (email),
        FOREIGN KEY (email) REFERENCES users(email)
    )
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS user_loan (
        email VARCHAR(255),
        aadhar_path VARCHAR(255),
        pan_path VARCHAR(255),
        passbook_path VARCHAR(255),
        passport_path VARCHAR(255),
        loan_amount BIGINT,
        FOREIGN KEY (email) REFERENCES users(email)
    )      
    """)