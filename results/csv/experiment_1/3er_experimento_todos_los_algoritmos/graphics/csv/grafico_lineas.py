import pandas as pd
import matplotlib.pyplot as plt

# === Archivo CSV ===
csv_file = "time.csv"

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

# === Gráfico con escala logarítmica ===
plt.figure(figsize=(14, 8))
for col in df.columns[1:]:  # omitir "Instance"
    plt.plot(df["Instance"], df[col], label=col)

plt.yscale("log")
plt.title("Tiempo de ejecución por instancia (escala logarítmica)")
plt.xlabel("Instancia")
plt.ylabel("Tiempo (segundos)")
plt.xticks(rotation=45)
plt.legend(loc="center left", bbox_to_anchor=(1.02, 0.5))
plt.tight_layout()
plt.grid(True, which="both", linestyle="--")
plt.show()