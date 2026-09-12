import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# === Datos reales ===
datos_reales = {
    'Algoritmo': ['FF', 'RE', 'TCC', 'UPGMC'],
    'Métrica': [3.969111111, 15.93672222, 4.339522222, 6.293211111],
    'Costo': [10.6111, 17.5556, 9.9444, 3.3333]
}

# === Datos normalizados ===
datos_norm = {
    'Algoritmo': ['FF', 'RE', 'TCC', 'UPGMC'],
    'Métrica': [0.579547116, 0.444444444, 0.550451259, 0.526671326],
    'Costo': [0.488282486, 0.0, 0.535159573, 1.0]
}

# === Crear DataFrames ===
df_reales = pd.DataFrame(datos_reales)
df_norm = pd.DataFrame(datos_norm)

# === Función para graficar comparaciones ===
def graficar_barras(df, titulo):
    x = np.arange(len(df['Algoritmo']))
    width = 0.35

    fig, ax = plt.subplots(figsize=(14, 6))
    ax.bar(x - width/2, df['Métrica'], width, label='Métrica')
    ax.bar(x + width/2, df['Costo'], width, label='Costo')

    ax.set_xlabel('Algoritmo')
    ax.set_ylabel('Valor')
    ax.set_title(titulo)
    ax.set_xticks(x)
    ax.set_xticklabels(df['Algoritmo'], rotation=45)
    ax.legend()
    ax.grid(axis='y', linestyle='--', alpha=0.5)

    plt.tight_layout()
    plt.show()

# === Graficar ambos conjuntos ===
graficar_barras(df_reales, 'Comparación entre Métrica y Costo (Valores reales)')
graficar_barras(df_norm, 'Comparación entre Métrica y Costo (Valores normalizados)')
