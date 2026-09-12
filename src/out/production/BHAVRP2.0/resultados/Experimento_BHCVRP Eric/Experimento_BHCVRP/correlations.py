import pandas as pd
from scipy.stats import spearmanr, pearsonr

# === Datos ===
datos = {
    "Algoritmo": [
        "BCA", "BN", "CLARA", "CP", "CA", "FF", "KM", "NC",
        "ND", "PAM", "P", "RE", "S", "SW", "TCC", "UPGMC"
    ],
    "SSE": [12.75, 7.125, 10, 12.1111, 14.0556, 3.4722, 4.5694, 6.9167, 10.8889, 8.9722, 6.9167, 15.9722, 6.1111, 6.1806, 3.4306, 6.5278],
    "BH": [12.5278, 6.625, 11.0278, 9.8333, 14.3611, 5.1944, 5.5694, 6.3333, 11.2222, 9.1111, 6.3333, 15.9722, 5.3889, 5.5694, 6.4306, 4.5],
    "SSB": [11.8611, 7.0417, 10.0556, 9.3056, 14.6944, 4.25, 5.1258, 6.6111, 11.6667, 8.1389, 6.6111, 16, 6.2222, 6.2083, 4.7361, 7.4444],
    "CH": [12.75, 7.125, 9.6389, 11.6111, 14.1667, 3.4444, 4.5139, 6.9444, 10.9722, 9.0556, 6.9444, 16, 6.0833, 6.1528, 3.3472, 7.25],
    "D": [8.7778, 7.5972, 10.125, 7.3889, 13.8056, 5.5833, 5.5139, 8.5972, 12.1389, 10.1389, 8.5972, 15.6528, 7.7083, 7.4167, 5.1389, 1.8194],
    "CS": [13.0278, 6.7361, 10, 11.3472, 13.9722, 3.25, 4.2639, 7.3056, 10.9167, 8.4722, 7.3056, 15.9167, 6.4722, 6.4861, 3.3472, 7.1806],
    "DB": [12.1111, 6.9028, 11.3611, 10.1667, 14.25, 4.1389, 4.5694, 6.75, 11.1944, 9.2222, 6.75, 15.9722, 6.0278, 5.9306, 4.5417, 6.1111],
    "XB": [12.2778, 6.9306, 10.8056, 11.2778, 13.8333, 3.1944, 4.375, 6.8056, 10.8889, 8.4167, 6.8056, 15.9722, 6.1389, 6.3194, 4.0417, 7.9167],
    "RT": [12.2778, 6.9306, 10.8333, 11.25, 13.8333, 3.1944, 4.4028, 6.8056, 10.8889, 8.4167, 6.8056, 15.9722, 6.1389, 6.3194, 4.0417, 7.8889],
    "Costo": [3.6944, 6.3056, 11.0, 8.2222, 6.6389, 10.6111, 6.6944, 11.9583, 9.9167, 11.5278, 11.5694, 17.5556, 7.75, 8.9167, 9.9444, 3.3333]
}

# === Crear DataFrame
df = pd.DataFrame(datos)
df.set_index("Algoritmo", inplace=True)

# === Definir métricas a maximizar
metricas_max = ["SSB", "CH", "D", "CS"]

# === Normalizar las métricas
df_norm = pd.DataFrame(index=df.index)

for col in df.columns[:-1]:  # todas menos 'Costo'
    if col in metricas_max:
        df_norm[col] = (df[col] - df[col].min()) / (df[col].max() - df[col].min())
    else:
        df_norm[col] = (df[col].max() - df[col]) / (df[col].max() - df[col].min())

# === Normalizar el costo (a minimizar)
df_norm["Costo"] = (df["Costo"].max() - df["Costo"]) / (df["Costo"].max() - df["Costo"].min())

# === Calcular correlaciones por métrica vs costo
print("======= CORRELACIONES POR MÉTRICA VS COSTO =======")
for col in df_norm.columns[:-1]:  # todas menos 'Costo'
    spearman_corr, spearman_p = spearmanr(df_norm[col], df_norm["Costo"])
    pearson_corr, pearson_p = pearsonr(df_norm[col], df_norm["Costo"])
    
    print(f"\n📊 {col}")
    print(f"   Spearman: {spearman_corr:.4f} (p = {spearman_p:.4f})", end=' ')
    print("✅" if spearman_p < 0.05 else "⚠️")
    
    print(f"   Pearson : {pearson_corr:.4f} (p = {pearson_p:.4f})", end=' ')
    print("✅" if pearson_p < 0.05 else "⚠️")

print("\n✅ Comparaciones completadas.")
