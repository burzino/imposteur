"""Aiuto per il collaudo via adb: un comando = un'azione + il testo della schermata risultante.

Uso (da Git Bash):
  python -I tools/collaudo/ui.py <seriale> schermo
  python -I tools/collaudo/ui.py <seriale> tocca "<testo o content-desc>" [n-esimo, da 1]
  python -I tools/collaudo/ui.py <seriale> contiene "<testo>"   -> esce con 1 se assente
  python -I tools/collaudo/ui.py <seriale> indietro
  python -I tools/collaudo/ui.py <seriale> doppiotocco "<testo>"   -> due tap rapidi nello stesso punto
  python -I tools/collaudo/ui.py <seriale> tastiera              -> dice se la tastiera è aperta
  python -I tools/collaudo/ui.py <seriale> tienipremuto "<testo>" [ms] -> pressione lunga (default 900 ms)
L'adb si cerca in $ADB, altrimenti in %LOCALAPPDATA%/Android/Sdk/platform-tools/adb.exe.
"""
import os, re, subprocess, sys, time
import xml.etree.ElementTree as ET

ADB = os.environ.get("ADB") or os.path.join(
    os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk", "platform-tools", "adb.exe")


def adb(seriale, *args):
    return subprocess.run([ADB, "-s", seriale, *args], capture_output=True, text=True,
                          encoding="utf-8", errors="replace").stdout


def nodi(seriale):
    adb(seriale, "shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb(seriale, "shell", "cat", "/sdcard/ui.xml")
    xml = xml[xml.find("<"):] if "<" in xml else "<hierarchy/>"
    return list(ET.fromstring(xml).iter("node"))


def testi(nn):
    out = []
    for n in nn:
        t = n.get("text") or n.get("content-desc") or ""
        if t.strip():
            extra = " [x]" if n.get("checked") == "true" else ""
            extra += " [off]" if n.get("enabled") == "false" else ""
            out.append(t.strip() + extra)
    return out


def stampa_schermo(seriale):
    nn = nodi(seriale)
    pkg = {n.get("package") for n in nn}
    if "it.imposteur" not in pkg:
        print("!! app non in primo piano (schermo bloccato o altra app)")
    print(" | ".join(testi(nn)))


def centro(b):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", b))
    return (x1 + x2) // 2, (y1 + y2) // 2


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    seriale, cmd, *rest = sys.argv[1:]
    if cmd == "schermo":
        stampa_schermo(seriale)
    elif cmd == "tocca":
        cerca, n = rest[0], int(rest[1]) if len(rest) > 1 else 1
        trovati = [x for x in nodi(seriale)
                   if cerca in ((x.get("text") or "") + "\n" + (x.get("content-desc") or ""))]
        if len(trovati) < n:
            print(f"!! non trovato: {cerca!r}")
            stampa_schermo(seriale)
            sys.exit(1)
        x, y = centro(trovati[n - 1].get("bounds"))
        adb(seriale, "shell", "input", "tap", str(x), str(y))
        time.sleep(1.0)
        stampa_schermo(seriale)
    elif cmd == "doppiotocco":
        trovati = [x for x in nodi(seriale)
                   if rest[0] in ((x.get("text") or "") + "\n" + (x.get("content-desc") or ""))]
        if not trovati:
            print(f"!! non trovato: {rest[0]!r}")
            sys.exit(1)
        x, y = centro(trovati[0].get("bounds"))
        adb(seriale, "shell", f"input tap {x} {y}; input tap {x} {y}")
        time.sleep(1.0)
        stampa_schermo(seriale)
    elif cmd == "tienipremuto":
        trovati = [x for x in nodi(seriale)
                   if rest[0] in ((x.get("text") or "") + "\n" + (x.get("content-desc") or ""))]
        if not trovati:
            print(f"!! non trovato: {rest[0]!r}")
            sys.exit(1)
        x, y = centro(trovati[0].get("bounds"))
        ms = rest[1] if len(rest) > 1 else "900"
        adb(seriale, "shell", "input", "swipe", str(x), str(y), str(x), str(y), ms)
        time.sleep(1.0)
        stampa_schermo(seriale)
    elif cmd == "tastiera":
        out = adb(seriale, "shell", "dumpsys", "input_method")
        print("tastiera aperta" if "mInputShown=true" in out else "tastiera chiusa")
    elif cmd == "contiene":
        ok = any(rest[0] in t for t in testi(nodi(seriale)))
        print("OK" if ok else f"!! assente: {rest[0]!r}")
        sys.exit(0 if ok else 1)
    elif cmd == "indietro":
        adb(seriale, "shell", "input", "keyevent", "4")
        time.sleep(1.0)
        stampa_schermo(seriale)


if __name__ == "__main__":
    main()
