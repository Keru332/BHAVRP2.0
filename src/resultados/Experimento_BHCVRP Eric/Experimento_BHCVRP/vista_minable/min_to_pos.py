import pandas as pd
import os

# === Ruta del archivo a corregir (en la misma carpeta del script)
archivo = os.path.join(os.path.dirname(__file__), "vista_minable_metricas.csv")

# === Cargar el archivo
df = pd.read_csv(archivo)

# === Corregir valores negativos en la columna 'costs'
if "costs" in df.columns:
    negativos = df[df["costs"] < 0].shape[0]
    df["costs"] = df["costs"].abs()
    print(f"✅ Corregidos {negativos} valores negativos en la columna 'costs'.")
else:
    print("⚠️ No se encontró la columna 'costs'.")

# === Guardar el archivo corregido
df.to_csv(archivo, index=False, encoding="utf-8-sig")
print(f"✅ Archivo actualizado: {archivo}")
