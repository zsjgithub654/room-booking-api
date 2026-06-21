# Demo Data
* To make it easier to explore the application, pre-defined users and rooms can be imported from [demo.sql](demo.sql). Check 
[Data Summary](#data-summary) for user credentials and room schedules.

## Import Demo Data
* The demo.sql contains both schema and data, and needs to be imported into an empty database.
* Create a database as required in [Data Source](../README.md#data-source). Before moving on, 
run the following command to import demo.sql into the database:
```powershell
psql -U your_user_name -d room_booking -f path_of_demo/demo.sql
```
* Do not start the application before import, otherwise the schema and bootstrap admin initialized by the application
will conflict with the ones in demo.sql. If it happens, clear the database and import again.
* After the import succeeds, configure and run the application as normal.

## Data Summary
### Users
* User 1 is an admin
* Users 3, 4 and 5 are regular users.
* User 2 is a closed account and can no longer log in.

| User ID | Username | Password   | Roles                     | Status |
|---------|----------|------------|---------------------------|--------|
| 1       | `user1`  | `11111111` | `ROLE_USER`, `ROLE_ADMIN` | active |
| 2       | `N/A`    | `N/A`      | `ROLE_USER`, `ROLE_ADMIN` | closed |
| 3       | `user3`  | `33333333` | `ROLE_USER`               | active |
| 4       | `user4`  | `44444444` | `ROLE_USER`               | active |
| 5       | `user5`  | `55555555` | `ROLE_USER`               | active |

### Rooms
* Room 4 is deleted and can only be seen by the admin.

| Room ID | Name   | Area         | Capacity | Open Hours           | Status  |
|---------|--------|--------------|----------|----------------------|---------|
| 1       | `A101` | `building A` | 4        | `00:00` - `23:59:59` | active  |
| 2       | `A102` | `building A` | 8        | `08:00` - `18:00`    | active  |
| 3       | `B101` | `building B` | 12       | `08:00` - `18:00`    | active  |
| 4       | `B102` | `building B` | 16       | `08:00` - `18:00`    | deleted |

### Past Schedules
All past schedules were on `2026-06-20`.

| Time              | `A101`                                | `A102`       | `B101`       | `B102 (deleted)` |
|-------------------|---------------------------------------|--------------|--------------|------------------|
| `00:00` - `07:00` |                                       | closed hours | closed hours | -                |
| `07:00` - `08:00` | reservation `#1`, user `1`, scheduled | closed hours | closed hours | -                |
| `08:00` - `09:00` | reservation `#1`, user `1`, scheduled |              |              | -                |
| `10:00` - `12:00` | closure `#1`                          |              |              | -                |
| `18:00` - `24:00` |                                       | closed hours | closed hours | -                |

### Future Schedules
All futurn schedules are on `2300-06-20`.

| Time              | `A101 (available all day)` | `A102 (closed all day)`                          | `B101`                                | `B102 (deleted)`                   |
|-------------------|----------------------------|--------------------------------------------------|---------------------------------------|------------------------------------|
| `00:00` - `08:00` |                            | closed hours                                     | closed hours                          | -                                  |
| `08:00` - `10:00` |                            | reservation `#2`, user `2`, closed; closure `#3` |                                       | -                                  |
| `10:00` - `12:00` |                            | closure `#3`                                     | reservation `#4`, user `3`, scheduled | -                                  |
| `12:00` - `14:00` |                            | closure `#3`                                     | reservation `#5`, user `4`, canceled  | -                                  |
| `14:00` - `16:00` |                            | closure `#3`                                     |                                       | reservation `#3`, user `4`, closed |
| `16:00` - `18:00` |                            | closure `#3`                                     |                                       | -                                  |
| `18:00` - `24:00` |                            | closed hours                                     | closed hours                          | -                                  |
