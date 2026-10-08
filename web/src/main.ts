import { mount } from "svelte";
import App from "./App.svelte";

const bersaglio = document.getElementById("app");
if (!bersaglio) throw new Error("Elemento #app mancante");

export default mount(App, { target: bersaglio });
