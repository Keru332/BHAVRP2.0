import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# === Archivo de entrada ===
input_file = "Resumen_Costos.csv"

# === Cargar CSV ===
df = pd.read_csv(input_file)

# === Transformar a formato largo (sin columna 'Instance') ===
df_melted = df.melt(var_name="Algoritmo", value_name="Costo")

# === Convertir a float (por si hay valores no numéricos) ===
df_melted["Costo"] = pd.to_numeric(df_melted["Costo"], errors="coerce")

# === Graficar violín ===
plt.figure(figsize=(16, 8))
sns.violinplot(data=df_melted, x="Algoritmo", y="Costo", inner="box", scale="width", linewidth=1)

plt.title("Distribución del costo total por algoritmo")
plt.ylabel("Costo total")
plt.xlabel("Algoritmo de asignación")
plt.xticks(rotation=45)
plt.grid(True, which="both", linestyle="--", linewidth=0.5)
plt.tight_layout()
plt.show()
