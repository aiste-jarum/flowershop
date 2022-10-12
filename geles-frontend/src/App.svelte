<script>
  import { user } from "./stores";
  import { Link, Route, navigate } from "svelte-routing";
  import axios from "axios";

  import AddFlower from "./AddFlower.svelte";
  import Cart from "./Cart.svelte";
  import FavoriteFlowers from "./FavoriteFlowers.svelte";
  import Home from "./Home.svelte";
  import Login from "./auth/Login.svelte";
  import Register from "./auth/Register.svelte";
  import Search from "./Search.svelte";
  import UpdateFlower from "./UpdateFlower.svelte";
  import Order from "./Order.svelte";
  import ManageOrders from "./ManageOrders.svelte";
  import Flower from "./Flower.svelte";
  import FavoriteFlowersStats from "./FavoriteFlowersStats.svelte";
  import EditOrder from "./EditOrder.svelte";
  import { getContext, onMount } from "svelte";
  import NotFound from "./NotFound.svelte";
  import { notificationContextKey } from "./contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  $: isLoggedIn = !!$user;
  $: isAdmin = $user && $user.admin;

  async function handleLogout() {
    try {
      await loading(
        "Atsijungiama...",
        (async () => {
          await axios.post("/auth/logout", null, {
            withCredentials: true
          });
          user.set(null);
          navigate("/");
        })()
      );
      success("Atsijungta");
    } catch (e) {
      error("Klaida atsijungiant");
    }
  }

  onMount(async () => {
    let oldUser: User | null;
    try {
      const response = await axios.get<User>("/users/", {
        withCredentials: true
      });
      oldUser = response.data;
    } catch (e) {
      oldUser = null;
    }
    user.set(oldUser);
  });
</script>

<!-- Wait until user was tried to be fetched -->
{#if $user !== undefined}
  <nav class="navbar">
    <Link class="button" to="/">Pagrindinis</Link>
    <Link class="button" to="/search">Paieška</Link>
    <!-- Logged in/logged out actions -->
    {#if !isLoggedIn}
      <Link class="button" to="/login">Prisijungti</Link>
      <Link class="button" to="/register">Užsiregistruoti</Link>
    {:else}
      <!-- Admin/non-admin actions -->
      {#if isAdmin}
        <Link class="button" to="/add">Pridėti gėlę</Link>
        <Link class="button" to="/favorite/stats">Mėgstamiausių statistika</Link
        >
      {:else}
        <Link class="button" to="/flowers/favorite">Mėgstamiausios gėlės</Link>
        <Link class="button" to="/cart">Krepšelis</Link>
      {/if}
      <Link class="button" to="/orders">Užsakymai</Link>
      <button class="button" on:click={handleLogout}>Atsijungti</button>
    {/if}
  </nav>
  <div>
    <Route path="/" component={Home} />
    <Route path="/search" component={Search} />
    <Route path="/flower/:flowerId" component={Flower} />
    {#if !isLoggedIn}
      <Route path="/login" component={Login} />
      <Route path="/register" component={Register} />
    {:else}
      {#if isAdmin}
        <Route path="/add" component={AddFlower} />
        <Route path="/update/:id" component={UpdateFlower} />
        <Route path="/favorite/stats" component={FavoriteFlowersStats} />
      {:else}
        <Route path="/cart" component={Cart} />
        <Route path="/flowers/favorite" component={FavoriteFlowers} />
        <Route path="/order/:cartId" component={Order} />
      {/if}

      <Route path="/orders" component={ManageOrders} />
      <Route path="/order/edit/:id" component={EditOrder} />
    {/if}
    <Route component={NotFound} />
  </div>
{/if}
