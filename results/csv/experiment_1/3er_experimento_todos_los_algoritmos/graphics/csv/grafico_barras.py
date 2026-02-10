import pandas as pd
import matplotlib.pyplot as plt

# === Archivo CSV ===
csv_file = "sse.csv"

# === Mapeo de nombres largos a siglas ===
algorithm_abbreviations = {
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
    "ThreeCriteriaClustering": "TCC",
    "UPGMC": "UPGMC"
}

# === Leer CSV y renombrar columnas ===
df = pd.read_csv(csv_file)
df = df.rename(columns=algorithm_abbreviations)

# === Calcular la media de SSE por algoritmo ===
mean_sse = df.drop(columns=["Instance"]).mean().sort_values()

# === Gráfico de barras ===
plt.figure(figsize=(14, 6))
bars = plt.bar(mean_sse.index, mean_sse.values, color='steelblue')
plt.xlabel("Algoritmo")
plt.xticks(rotation=45)
plt.grid(axis='y')

# === Mostrar valores sobre cada barra ===
for bar in bars:
    height = bar.get_height()
    plt.text(bar.get_x() + bar.get_width() / 2, height + 1000, f'{height:.0f}',
             ha='center', va='bottom', fontsize=8)

plt.tight_layout()
plt.show()
