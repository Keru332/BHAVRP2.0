import pandas as pd
import openpyxl

# === Rutas ===
archivo_entrada = "Resultados_MDVRP_Java.xlsx"
archivo_salida = "Resumen_Costos.xlsx"

# === Leer todas las hojas ===
xls = pd.read_excel(archivo_entrada, sheet_name=None)

# === Detectar algoritmos desde una hoja de ejemplo ===
hoja_ejemplo = next(iter(xls.values()))
algoritmos = list(hoja_ejemplo["BHAVRP"].dropna().unique())

# === Crear DataFrame vacío ===
instancias = list(xls.keys())
df_resumen = pd.DataFrame(index=algoritmos, columns=instancias)

# === Llenar tabla con los costos por algoritmo en cada instancia ===
for instancia, df in xls.items():
    if "BHAVRP" in df.columns and "Costo euclidean" in df.columns:
        for _, fila in df.iterrows():
            algoritmo = fila["BHAVRP"]
            costo = fila["Costo euclidean"]
            if pd.notna(algoritmo) and pd.notna(costo):
                df_resumen.at[algoritmo, instancia] = round(float(costo), 2)

# === Guardar como nuevo Excel ===
df_resumen.to_excel(archivo_salida)

print(f"✅ Archivo '{archivo_salida}' generado correctamente.")
