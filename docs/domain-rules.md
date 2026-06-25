# Domain Rules

## Main Domain Objects

- `User`
  - account owner with roles and status
- `Room`
  - bookable resource with area, capacity, and open hours
- `Reservation`
  - scheduled booking of a room by a user
- `Closure`
  - blocked room time range

## Roles And Access Scope

- public users
  - can register
  - can search room availability
- authenticated normal users
  - can view and update their own account
  - can create, view, update, and release their own reservations
  - can view active rooms
- admin users
  - can manage users, rooms, reservations, and closures
  - can search across all users and reservations
  - can view deleted rooms

## User Rules

### Roles

- every new user starts with `ROLE_USER`
- `ROLE_ADMIN` can be added or removed later
- the last active admin cannot lose the admin role
- the last active admin cannot be closed

### Statuses

- `USER_STATUS_ACTIVE`
  - normal usable account
- `USER_STATUS_CLOSED`
  - closed account kept only for history and foreign-key references

### Username And Password Rules

- usernames must be unique
- passwords are stored encoded

### Close Account Behavior

Closing a user account does not delete the row. Instead, the system:
- changes status to `USER_STATUS_CLOSED`
- clears username and password
- closes the user’s future scheduled reservations

Closed users:
- cannot log in
- cannot update username or password
- cannot receive new reservations
- cannot gain or lose admin role
- are visible to admins

## Room Rules

### Statuses

- `ROOM_STATUS_ACTIVE`
  - normal bookable room
- `ROOM_STATUS_DELETED`
  - removed from normal use, but still kept for history

### Open Hours

- a room can have explicit open and close times
- if both open time and close time are omitted, the room is treated as open all day
- if open hours are configured, reservations must stay within those hours
- changing open hours does not affect scheduled reservations

### Delete Room Behavior

Deleting a room does not remove the row.

Instead, the system:
- changes status to `ROOM_STATUS_DELETED`
- deletes future closures for that room
- closes future scheduled reservations for that room

Deleted rooms:
- cannot be updated
- are hidden from normal users
- are still visible to admins
- are excluded from availability search

## Reservation Rules

### Statuses

- `RESERVATION_STATUS_SCHEDULED`
  - reservations are booked successfully, either started or upcoming
- `RESERVATION_STATUS_CANCELED`
  - released by the user or admin before start
- `RESERVATION_STATUS_CLOSED`
  - reservations are closed because of room closure, room deletion, or user closure

### Create Rules

To create a reservation:
- the user must exist and be active
- the room must exist and be active
- the requested time must fit the room open hours
- the requested time must not overlap an existing scheduled reservation for the room
- the requested time must not overlap a closure for the room

### Update Rules

Only reservation time can be updated.

A reservation cannot be updated if it is:
- canceled
- closed
- already started

When the new time extends outside the original interval, the system re-checks:
- room open hours
- overlapping closures
- overlapping scheduled reservations

### Release Rules

- if the reservation already ended, release does nothing
- if the reservation is ongoing, it ends at the nearest minute. The remaining time is release.
- if the reservation has not started yet, it will be canceled. Status becomes `RESERVATION_STATUS_CANCELED`

## Closure Rules

closure endpoints are admin-only

### Create Rules

To create a closure:
- the room must exist and be active
- future overlapping scheduled reservations are changed to `RESERVATION_STATUS_CLOSED`
- if an overlapping reservation in ongoing, its end time is shortened to the closure start time
- Overlapping and adjacent closures are merged.

### Delete Rules

- past closures cannot be deleted

## Concurrent Update Behavior

- user and room records are locked during reservation, room-delete, user-close, and closure-create flows where competing writes would otherwise break booking consistency
- entities also use optimistic locking for concurrent updates and deletes

As a result:
- competing operations may cause one request to fail
- final persisted state should still remain consistent
- concurrency behavior is covered by dedicated integration tests
