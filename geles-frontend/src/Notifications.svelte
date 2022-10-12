<script>
  import { AppNotificationType } from "./enums";

  import spinner from "./assets/spinners.svg";
  import { fly } from "svelte/transition";

  export let notifications: AppNotification[];
  export let onClick: (id: number) => boolean;

  function getNotificationIcon(type: AppNotificationType) {
    switch (type) {
      case AppNotificationType.SUCCESS:
        return "mdi-check white";
      case AppNotificationType.DANGER:
        return "mdi-alert-circle white";
      case AppNotificationType.WARNING:
        return "mdi-alert black";
      case AppNotificationType.INFO:
        return "mdi-information black";
    }
  }

  function getNotificationBackgroundColor(type: AppNotificationType) {
    switch (type) {
      case AppNotificationType.SUCCESS:
        return "success-bg";
      case AppNotificationType.DANGER:
        return "danger-bg";
      case AppNotificationType.WARNING:
        return "warning-bg";
      case AppNotificationType.INFO:
        return "info-bg";
    }
  }

  function getNotificationColor(type: AppNotificationType) {
    switch (type) {
      case AppNotificationType.SUCCESS:
      case AppNotificationType.DANGER:
        return "white";
      case AppNotificationType.WARNING:
      case AppNotificationType.INFO:
        return "black";
    }
  }
</script>

<div class="notifications">
  {#each notifications as { id, loading, text, type } (id)}
    <div
      class="notification {!loading
        ? getNotificationBackgroundColor(type)
        : 'theme-primary-light-bg'} {getNotificationColor(type)}"
      transition:fly={{ x: 500 }}
      on:click={() => {
        if (!loading) {
          onClick(id);
        }
      }}
    >
      {#if !loading}
        <i class="mdi {getNotificationIcon(type)}" />
      {/if}
      {text}
      <div style="flex-grow:1;" />
      {#if loading}
        <img
          style="width:24px;height:24px;"
          class="flex-right"
          src={spinner}
          alt="loading"
        />
      {/if}
    </div>
  {/each}
</div>

<style>
  .notifications {
    position: fixed;
    top: 16px;
    right: 16px;
    z-index: 100;
    padding: 8px;
    /* background-color: rgba(255, 255, 255, 1); */
  }

  .notification {
    width: 300px;
    border: 1px solid rgb(0, 0, 0);
    padding: 8px 16px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
  }

  .notification:not(:last-child) {
    margin-bottom: 16px;
  }

  .flex-right {
    justify-self: flex-end;
  }
</style>
