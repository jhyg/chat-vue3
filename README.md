## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Supabase

1. Install Supabase
	macOs		
    ``` 
    brew install supabase/tap/supabase
    brew upgrade supabase
    ```
	windows
    ```
    scoop bucket add supabase https://github.com/supabase/scoop-bucket.git
    scoop install supabase
    scoop update supabase
    ```

2. Start Docker Desktop 

3. Start Supabase
	```
    supabase init
    supabase start
	```

4. Connect Supabase to supabase.js
    - supabaseUrl: {{ API URL }}
    - supabaseKey: {{ anon key }}

5. Check Supabase Dashboard: http://127.0.0.1:54323

6. Create Tables
    Access the dashboard, go to SQL editor, enter the following SQL and execute:
    ```
    CREATE TABLE IF NOT EXISTS chat_rooms (
        room_id VARCHAR(255) PRIMARY KEY,
        room_pw VARCHAR(255),
        room_name VARCHAR(255),
        is_private BOOLEAN DEFAULT FALSE
    );

    CREATE TABLE IF NOT EXISTS messages (
        message_id VARCHAR(255) PRIMARY KEY,
        room_id VARCHAR(255),
        user_id VARCHAR(255),
        user_name VARCHAR(255),
        content TEXT,
        timestamp TIMESTAMP DEFAULT NOW()
    );
    ```

7. Enable Realtime for messages table
    After creating the tables, go to the Table editor in the dashboard, click on the messages table, and click the "Realtime off" button in the top right corner to change it to "on".

9. Stop Supabase
    ```
    supabase stop
    ```
