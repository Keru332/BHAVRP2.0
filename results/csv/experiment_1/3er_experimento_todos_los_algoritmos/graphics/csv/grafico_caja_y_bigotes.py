import pandas as pd
import matplotlib.pyplot as plt

# === Archivo CSV ===
csv_file = "sse.csv"

# === Mapeo a siglas ===
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

# === Leer y transformar ===
df = pd.read_csv(csv_file)
df = df.rename(columns=algorithm_abbreviations)

# === Gráfico boxplot clásico con siglas ===
plt.figure(figsize=(14, 8))
df_boxplot = df.drop(columns=["Instance"])
df_boxplot.boxplot()
plt.xticks(rotation=45)
plt.grid(True)
plt.tight_layout()
plt.show()