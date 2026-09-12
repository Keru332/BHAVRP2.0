import pandas as pd

# === Paso 1: Tabla con características de instancias (puedes moverlo a CSV si prefieres)
datos_instancias = [
    # Cordeau
    ("p01", 50, 4, 4, 80), ("p02", 50, 4, 2, 160), ("p03", 75, 5, 3, 140), ("p04", 100, 2, 8, 100),
    ("p05", 100, 2, 5, 200), ("p06", 100, 3, 6, 100), ("p07", 100, 4, 4, 100), ("p08", 249, 2, 14, 500),
    ("p09", 249, 3, 12, 500), ("p10", 249, 4, 8, 500), ("p11", 249, 5, 6, 500),
    ("p12", 80, 2, 5, 60), ("p13", 80, 2, 5, 60), ("p14", 80, 2, 5, 60),
    ("p15", 160, 4, 5, 60), ("p16", 160, 4, 5, 60), ("p17", 160, 4, 5, 60),
    ("p18", 240, 6, 5, 60), ("p19", 240, 6, 5, 60), ("p20", 240, 6, 5, 60),
    ("p21", 360, 9, 5, 60), ("p22", 360, 9, 5, 60), ("p23", 360, 9, 5, 60),
    ("pr1", 48, 4, 1, 200), ("pr2", 96, 4, 2, 195), ("pr3", 144, 4, 3, 190),
    ("pr4", 192, 4, 4, 185), ("pr5", 240, 4, 5, 180), ("pr6", 288, 4, 6, 175),
    ("pr7", 72, 1, 6, 200), ("pr8", 144, 2, 6, 190), ("pr9", 216, 6, 3, 180), ("pr10", 288, 6, 4, 170),
    # Gillett & Johnson
    ("gj1", 50, 2, 2, 492), ("gj2", 75, 3, 3, 415), ("gj3", 100, 2, 2, 841), ("gj4", 150, 4, 4, 651),
    ("gj5", 199, 5, 5, 758), ("gj6", 75, 2, 2, 669), ("gj7", 125, 3, 3, 768),
    ("gj8", 249, 4, 4, 1219), ("gj9", 60, 2, 2, 579), ("gj10", 80, 3, 3, 507), ("gj11", 120, 5, 5, 1000),
]

columnas_instancia = ["Instance", "Clientes", "Depositos", "VehiculosPorDeposito", "Capacidad"]
df_instancias = pd.DataFrame(datos_instancias, columns=columnas_instancia)

# === Paso 2: Cargar el archivo con métricas
archivo_metricas = "vista_minable_metricas.csv"
df_metricas = pd.read_csv(archivo_metricas)

# === Paso 3: Obtener el algoritmo con menor costo por instancia
df_metricas_filtrado = df_metricas[["Instance", "Algorithm", "costs"]].copy()
df_metricas_filtrado = df_metricas_filtrado.dropna(subset=["costs"])
df_metricas_filtrado["costs"] = df_metricas_filtrado["costs"].abs()  # aseguramos que el costo sea positivo

# Elegir el algoritmo con menor costo para cada instancia
df_ganadores = df_metricas_filtrado.loc[df_metricas_filtrado.groupby("Instance")["costs"].idxmin()]
df_ganadores = df_ganadores.rename(columns={"Algorithm": "Ganador"})

# === Paso 4: Unir con la tabla de características
df_final = pd.merge(df_instancias, df_ganadores[["Instance", "Ganador"]], on="Instance", how="left")

# === Paso 5: Guardar resultado
salida = "vista_minable_caracteristicas.csv"
df_final.to_csv(salida, index=False)
print(f"✅ Vista minable generada correctamente: {salida}")
