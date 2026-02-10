import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# === Archivo de entrada ===
input_file = "dunnindex.csv"          # Ajusta la ruta si es necesario

# === Cargar CSV ===
df = pd.read_csv(input_file)

# === Mapeo de nombres largos a siglas (solo estos cuatro algoritmos se mostrarán) ===
abbreviations = {
    "RandomByElement": "RE",
    "Farthest_First": "FF",
    "ThreeCriteriaClustering": "TCC",
    "UPGMC": "UPGMC"
}

# === Renombrar columnas (si existen) ===
df = df.rename(columns=abbreviations)

# === Transformar a formato largo (tidy) ===
df_melted = df.melt(id_vars=["Instance"], var_name="Algoritmo", value_name="Valor")

# === Convertir valores a float (coerce convierte no numéricos en NaN) ===
df_melted["Valor"] = pd.to_numeric(df_melted["Valor"], errors="coerce")

# === Filtrar solo los algoritmos deseados (siglas en el dict) ===
algoritmos_deseados = list(abbreviations.values())
df_melted = df_melted[df_melted["Algoritmo"].isin(algoritmos_deseados)]

# === Graficar ===
plt.figure(figsize=(16, 8))
sns.violinplot(
    data=df_melted,
    x="Algoritmo",
    y="Valor",
    inner="box",
    density_norm="width",   # Sustituye a scale="width" (evita FutureWarning)
    linewidth=1
)

plt.title("Distribución del Índice de Dunn (escala lineal)")
plt.ylabel("Valor (escala lineal)")
plt.xlabel("Algoritmo")
plt.xticks(rotation=45)
plt.grid(True, which="both", linestyle="--", linewidth=0.5)
plt.tight_layout()

# === Mostrar o guardar ===
plt.show()
# Si prefieres guardar automáticamente:
# plt.savefig("violin_dunn_algoritmos.png", dpi=300)
