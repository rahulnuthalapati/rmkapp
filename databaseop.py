def checkdb(cursor):
    cursor.execute("CREATE DATABASE IF NOT EXISTS rmkdb")
    cursor.execute("USE rmkdb")
    cursor.execute("CREATE TABLE IF NOT EXISTS users (name VARCHAR(255), email VARCHAR(255), phone INT(10), password VARCHAR(20), dob DATE, aadharno INT(12), bankaccno INT(20), PRIMARY KEY (email), UNIQUE (Aadharno), UNIQUE (bankaccno))")
    cursor.execute("CREATE TABLE IF NOT EXISTS user_business (name VARCHAR(255), address VARCHAR(255), type VARCHAR(255), monthly_revenue INT(10), annual_revenue INT(10), monthly_expense INT(10), annual_expense INT(10), profit_margin INT(10), PRIMARY KEY (name))")
    cursor.execute("CREATE TABLE IF NOT EXISTS user_income_savings (email VARCHAR(255), monthly_income INT(10), annual_income INT(10), sources VARCHAR(255), savings_balance INT(10), savings_acc_details VARCHAR(255), monthly_savings_contributions INT(10), PRIMARY KEY (email), FOREIGN KEY (email) REFERENCES users(email))")
    cursor.execute("CREATE TABLE IF NOT EXISTS feedback (email VARCHAR(255), feedback VARCHAR(255), PRIMARY KEY (email), FOREIGN KEY (email) REFERENCES users(email))")