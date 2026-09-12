import pandas as pd
import os

# === Directorio actual
carpeta = os.path.dirname(__file__)

# === Mapeo de nombres largos a siglas (como en costs.csv)
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
    "ThreeCriteriaClustering": "TCC",
    "UPGMC": "UPGMC"
}

# === Métricas esperadas
metricas = [
    "sse", "ballhallindex", "ssb", "calinskiharabaszindex", "dunnindex", 
    "silhouettecoefficient", "daviesbouldinindex", "xiebeniinindex", 
    "rayturiindex", "time", "costs"
]

# === Leer todos los CSV
metric_data = {}
for metrica in metricas:
    path = os.path.join(carpeta, f"{metrica}.csv")
    if os.path.exists(path):
        df = pd.read_csv(path)
        metric_data[metrica] = df
    else:
        print(f"⚠️ Archivo no encontrado: {metrica}.csv")

# === Usar la estructura de uno de los archivos como base
algoritmos = metric_data["sse"].iloc[:, 0].tolist()
instancias = metric_data["sse"].columns[1:].tolist()

# === Construir estructura base
datos = []
for instancia in instancias:
    for alg in algoritmos:
        sigla = abbreviations.get(alg, alg)
        fila = {
            "Instance": instancia,
            "Algorithm": sigla
        }
        datos.append(fila)

df_final = pd.DataFrame(datos)

# === Insertar cada métrica
for metrica, df in metric_data.items():
    df_temp = df.set_index(df.columns[0]).T
    df_temp.columns = [abbreviations.get(c, c) for c in df_temp.columns]
    df_temp = df_temp.reset_index().rename(columns={"index": "Instance"})
    df_long = df_temp.melt(id_vars="Instance", var_name="Algorithm", value_name=metrica)
    df_final = pd.merge(df_final, df_long, on=["Instance", "Algorithm"], how="left")

# === Determinar si es el mejor (mínimo) en costo por instancia
df_final["Ganador"] = df_final.groupby("Instance")["costs"].transform("min") == df_final["costs"]
df_final["Ganador"] = df_final["Ganador"].map({True: "Sí", False: "No"})

# === Guardar
salida = os.path.join(carpeta, "vista_minable_metricas.csv")
df_final.to_csv(salida, index=False, encoding="utf-8-sig")

print(f"✅ Archivo generado correctamente: {salida}")
