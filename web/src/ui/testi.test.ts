import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { t } from "./testi";

const xml = readFileSync(new URL("../../../app/src/main/res/values/strings.xml", import.meta.url), "utf8");

const camel = (n: string): string => n.replace(/_([a-z0-9])/g, (_, c: string) => c.toUpperCase());
const chiavi = (tag: string): string[] =>
  [...xml.matchAll(new RegExp(`<${tag} name="([^"]+)"`, "g"))].map((m) => m[1]);
const tt = t as Record<string, unknown>;

describe("testi", () => {
  it("ogni <string> di strings.xml ha la sua chiave", () => {
    const mancanti = chiavi("string").filter((k) => !(camel(k) in t));
    expect(mancanti).toEqual([]);
  });

  it("ogni <plurals> e <string-array> ha la sua chiave", () => {
    for (const k of chiavi("plurals")) expect(typeof tt[camel(k)]).toBe("function");
    for (const k of chiavi("string-array")) expect(Array.isArray(tt[camel(k)])).toBe(true);
  });

  it("testi identici ad Android (apostrofi, segnaposto, plurali)", () => {
    expect(t.regoleFine).toContain("l'impostore");
    expect(t.regoleFine).not.toContain("\\");
    expect(t.configGiocatoreN(3)).toBe("Giocatore 3");
    expect(t.configParoleRimanenti(2, 9)).toBe("Parole ancora da giocare: 2 / 9");
    expect(t.distribuzionePassa("Anna")).toBe("Passa il telefono a Anna");
    expect(t.rivelaImpostore("Bea")).toBe("L'impostore era: Bea");
    expect(t.opzAttive(1)).toBe("1 attiva");
    expect(t.opzAttive(3)).toBe("3 attive");
    expect(t.opzBreveGiri(2)).toBe("2 giri");
  });

  it("testi nuovi del web", () => {
    expect(t.esportaSegnalazioni).toBe("Esporta segnalazioni");
    expect(t.condividiSegnalazioni).toBe("Condividi");
    expect(t.segnalazioniSalvateN(1)).toBe("1 segnalazione salvata");
    expect(t.segnalazioniSalvateN(4)).toBe("4 segnalazioni salvate");
    expect(t.installaIos).toBe("Per installare l'app su iPhone: tocca Condividi, poi Aggiungi a Home.");
  });
});

// ---- Confronto testo per testo con strings.xml (CA-36, CA-W18) -------------------------------

const ENTITA: Record<string, string> = { "&amp;": "&", "&lt;": "<", "&gt;": ">", "&quot;": '"', "&apos;": "'" };

/** Interpreta il testo di un nodo di strings.xml come fa Android: spazi, virgolette ed escape. */
function testoAndroid(grezzo: string): string {
  let r = grezzo.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, "$1").trim();
  r = r.replace(/&(amp|lt|gt|quot|apos);/g, (m) => ENTITA[m] ?? m);
  // gli a capo reali nel file collassano in uno spazio; \n scritto come escape resta un a capo
  r = r.replace(/\s*\n\s*/g, " ");
  if (r.length >= 2 && r.startsWith('"') && r.endsWith('"')) r = r.slice(1, -1);
  return r.replace(/\\(u[0-9a-fA-F]{4}|.)/g,(_, c: string) => {
    if (c === "n") return "\n";
    if (c === "t") return "\t";
    if (c.startsWith("u") && c.length === 5) return String.fromCharCode(parseInt(c.slice(1), 16));
    return c;
  });
}

const SEGNAPOSTO = /%(?:(\d+)\$)?([ds])|%%/g;

/** Argomenti di prova per un testo con segnaposto e il testo atteso dopo la sostituzione. */
function argomentiEAttesi(modello: string): { args: Array<number | string>; atteso: string } {
  const args: Array<number | string> = [];
  let sequenza = 0;
  const atteso = modello.replace(SEGNAPOSTO, (m, pos: string | undefined, tipo: string | undefined) => {
    if (m === "%%") return "%";
    const i = pos !== undefined ? Number(pos) - 1 : sequenza++;
    args[i] = tipo === "d" ? 7 + i : `Nome${i + 1}`;
    return String(args[i]);
  });
  return { args, atteso };
}

const nodi = (tag: string): Array<{ nome: string; corpo: string }> =>
  [...xml.matchAll(new RegExp(`<${tag} name="([^"]+)"[^>]*>([\\s\\S]*?)</${tag}>`, "g"))].map((m) => ({
    nome: m[1]!,
    corpo: m[2]!,
  }));

describe("testi: stesso testo di strings.xml (CA-36, CA-W18)", () => {
  it("CA-36 ogni <string> ha la chiave convertita con lo stesso testo (segnaposto a parte)", () => {
    const stringhe = nodi("string");
    expect(stringhe.length).toBeGreaterThan(100);
    const diversi: string[] = [];
    for (const { nome, corpo } of stringhe) {
      const modello = testoAndroid(corpo);
      const valore = tt[camel(nome)];
      const { args, atteso } = argomentiEAttesi(modello);
      let ottenuto: unknown;
      if (args.length === 0) ottenuto = valore;
      else if (typeof valore === "function") ottenuto = (valore as (...a: unknown[]) => unknown)(...args);
      else ottenuto = `<non e' una funzione: ${typeof valore}>`;
      if (ottenuto !== atteso) diversi.push(`${nome}: atteso ${JSON.stringify(atteso)}, ottenuto ${JSON.stringify(ottenuto)}`);
    }
    expect(diversi).toEqual([]);
  });

  it("CA-36 nessun testo contiene backslash residui", () => {
    const conBackslash: string[] = [];
    for (const [chiave, valore] of Object.entries(tt)) {
      if (typeof valore === "string" && valore.includes("\\")) conBackslash.push(chiave);
      if (typeof valore === "function") {
        try {
          const f = valore as (...a: unknown[]) => unknown;
          const r = f.length >= 2 ? f(3, 4) : f(3);
          if (typeof r === "string" && r.includes("\\")) conBackslash.push(chiave);
        } catch {
          // funzione con argomenti non numerici: coperta dal confronto con strings.xml
        }
      }
    }
    expect(conBackslash).toEqual([]);
  });

  it("CA-36 ogni <plurals> ha una funzione che restituisce le forme del file (one, other)", () => {
    const plurali = nodi("plurals");
    expect(plurali.length).toBeGreaterThan(0);
    for (const { nome, corpo } of plurali) {
      const f = tt[camel(nome)] as (n: number) => string;
      expect(typeof f).toBe("function");
      const forme = new Map<string, string>();
      for (const m of corpo.matchAll(/<item quantity="([^"]+)"[^>]*>([\s\S]*?)<\/item>/g)) {
        forme.set(m[1]!, testoAndroid(m[2]!));
      }
      const prova = (n: number, forma: string): void => {
        const modello = forme.get(forma) ?? forme.get("other");
        expect(modello, `${nome}: forma ${forma}`).toBeDefined();
        expect(f(n), `${nome}(${n})`).toBe(modello!.replace(/%(?:\d+\$)?d/g, String(n)));
      };
      if (forme.has("one")) prova(1, "one");
      prova(5, "other");
      prova(0, forme.has("zero") ? "zero" : "other");
    }
  });

  it("CA-36 ogni <string-array> ha un array con gli stessi elementi", () => {
    for (const { nome, corpo } of nodi("string-array")) {
      const attesi = [...corpo.matchAll(/<item[^>]*>([\s\S]*?)<\/item>/g)].map((m) => testoAndroid(m[1]!));
      expect(tt[camel(nome)], nome).toEqual(attesi);
    }
  });

  it("CA-36 i testi hanno apostrofi senza backslash (regole, rivela)", () => {
    expect(t.regoleModalitaSenzaParola).toContain("l'impostore");
    expect(t.regoleModalitaSenzaParola).not.toContain("\\");
    expect(t.regoleFine).toContain("l'impostore");
    expect(t.rivelaImpostore("Bea")).toBe("L'impostore era: Bea");
  });
});
