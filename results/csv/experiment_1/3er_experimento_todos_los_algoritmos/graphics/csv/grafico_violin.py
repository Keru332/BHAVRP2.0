import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# === Archivo de entrada ===
input_file = "dunnindex.csv"

# === Cargar CSV ===
df = pd.read_csv(input_file)

# === Mapeo de nombres largos a siglas ===
abbreviations = {
    "BestCyclicAssignment": "BCA",
    "BestNearest": "BN",
    "CLARA": "CLARA",
    "CoefficientPropagation": "CP",
    "CyclicAssignment": "CA",
    "Farthest_First": "FF",
    "KMEANS": "KM",
    "NearestByCustomer": "NC",
    "NearestByDepot": "ND",
    "PAM": "PAM",
    "Parallel": "P",
    "RandomByElement": "RE",
    "RandomSequentialCyclic": "RSC",
    "SequentialCyclic": "SC",
    "Simplified": "S",
    "Sweep": "SW",
    "RandomByElement": "RE",
    "Farthest_First": "FF",
    "ThreeCriteriaClustering": "TCC",
    "UPGMC": "UPGMC"
}

# === Renombrar columnas ===
df = df.rename(columns=abbreviations)

# === Transformar a formato largo ===
df_melted = df.melt(id_vars=["Instance"], var_name="Algoritmo", value_name="Valor")

# === Convertir valores a float, ignorar errores como '-' ===
df_melted["Valor"] = pd.to_numeric(df_melted["Valor"], errors="coerce")

# === Graficar ===
plt.figure(figsize=(16, 8))
sns.violinplot(data=df_melted, x="Algoritmo", y="Valor", inner="box", scale="width", linewidth=1)
#plt.yscale("log")  # Escala logarítmica para visibilizar todas las curvas
#plt.title("Distribución del índice de (escala logarítmica)")
plt.title("Distribución (escala líneal)")
#plt.ylabel("Valor (escala logarítmica)")
plt.ylabel("Valor (escala líneal)")
plt.xlabel("Algoritmo")
plt.xticks(rotation=45)
plt.grid(True, which="both", linestyle="--", linewidth=0.5)
plt.tight_layout()
plt.show()