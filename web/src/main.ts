import { mount } from "svelte";
import App from "./App.svelte";
import "./ui/tema.css";

const bersaglio = document.getElementById("app");
if (!bersaglio) throw new Error("Elemento #app mancante");

export default mount(App, { target: bersaglio });
