import pandas as pd
from scipy.stats import spearmanr, pearsonr

# === Leer archivo generado ===
df = pd.read_csv("MetricaGlobal_vs_Costo.csv", index_col=0)

# === Obtener vectores
metricas = df["MetricaGlobal"]
costos = df["Costo"]

# === Spearman (rangos)
spearman_corr, spearman_p = spearmanr(metricas, costos)

# === Pearson (lineal)
pearson_corr, pearson_p = pearsonr(metricas, costos)

# === Mostrar resultados
print("===== CORRELACIONES =====")
print(f"Spearman: {spearman_corr:.4f} (p = {spearman_p:.4f})")
print(f"Pearson : {pearson_corr:.4f} (p = {pearson_p:.4f})")

# === Interpretación básica
if spearman_p < 0.05:
    print("✅ Correlación de Spearman estadísticamente significativa (p < 0.05)")
else:
    print("⚠️ Spearman no significativa")

if pearson_p < 0.05:
    print("✅ Correlación de Pearson estadísticamente significativa (p < 0.05)")
else:
    print("⚠️ Pearson no significativa")
