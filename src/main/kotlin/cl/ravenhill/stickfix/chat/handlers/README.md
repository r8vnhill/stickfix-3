### `handlers` Package - README

The `handlers` package in StickFix provides a set of interfaces that define the common interactions and state
transitions for users within the StickFix application. Each interface corresponds to a specific type of user
interaction, such as starting a session, handling private mode, or managing shuffle mode. These interfaces allow the
StickFix bot to handle complex user interactions by delegating the logic to the user's current state.

#### Interfaces Overview

1. **`IdleHandler`**:
    - **Purpose**: Manages the transition of a user to the idle state. This interface ensures that the user's state is
      properly set to idle by delegating the logic to the user's current state.
    - **Usage**: Implemented by user classes that need to support transitioning to an idle state.

2. **`PrivateModeHandler`**:
    - **Purpose**: Handles enabling and disabling private mode for the user. The actual enabling/disabling logic is
      managed by the user's current state.
    - **Usage**: Useful for managing user privacy settings within StickFix, allowing users to toggle private mode.

3. **`RevokationHandler`**:
    - **Purpose**: Manages the revocation process for users, including confirming or rejecting a revocation request. The
      revocation logic is delegated to the current state.
    - **Usage**: Applied to user classes that need to handle revocation of user data or permissions within StickFix.

4. **`ShuffleHandler`**:
    - **Purpose**: Manages the shuffle-related actions, including enabling, disabling, and executing the shuffle
      functionality. This interface delegates the shuffle logic to the user's current state.
    - **Usage**: Implemented by user classes that support sticker shuffling features within StickFix.

5. **`StartHandler`**:
    - **Purpose**: Handles the start process for a user, including confirming or rejecting a start action. The logic for
      handling these actions is managed by the user's current state.
    - **Usage**: Used by classes that manage the initiation of interactions or sessions within StickFix.

6. **`Stateful`**:
    - **Purpose**: A base interface that ensures any implementing class is associated with a `State`. It is used to 
      provide a common structure for all handlers that involve state transitions.
    - **Usage**: Implemented by all handler interfaces to maintain consistency in state management.

#### Usage Scenario

The interfaces provided in the `handlers` package are intended to be implemented by classes representing users within
StickFix, such as the `StickfixUser` class. These interfaces ensure that the user's actions and transitions are handled
in a consistent and state-aware manner.

Example:

```kotlin
data class StickfixUser(
    val username: String,
    override val id: Long,
) : StickfixChat, StartHandler, IdleHandler, RevokationHandler, PrivateModeHandler, ShuffleHandler {

    override var state: State = IdleState(this)
    override val debugInfo: String get() = if (username.isNotBlank()) "'$username'" else id.toString()

    override fun toString() = "StickfixUser(username='$username', id=$id, state=${state::class.simpleName})"

    companion object {
        fun from(from: TelegramUser) = StickfixUser(from.username ?: "unknown", from.id)
    }
}
```

In the example above, `StickfixUser` implements multiple handler interfaces, allowing it to manage various aspects of
user interaction such as starting a session, revoking permissions, and toggling private mode. Each handler interface
provides a set of methods that are delegated to the user's current state, ensuring that the logic specific to the
current context is executed.

This design allows StickFix to handle complex user interactions in a modular and state-aware manner, making it easier to
extend and maintain the application's functionality.