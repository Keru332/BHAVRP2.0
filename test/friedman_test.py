import numpy as np
from scipy.stats import friedmanchisquare

# Datos de tiempos de ejecución de las heurísticas
# Cada fila corresponde a una ejecución y cada columna a un algoritmo
data = np.array([
    [918, 65, 3, 4787, 3, 1, 102, 1, 1, 9771, 1, 1, 1, 1, 1, 1, 1, 2],
    [2, 22, 1, 1759, 2, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 86],
    [2, 125, 0, 260, 1, 1, 10, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0],
    [2, 131, 89, 5041, 2, 1, 106, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1],
    [3, 68, 0, 1906, 1, 0, 10, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1],
    [1, 121, 1, 1693, 2, 0, 114, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1],
    [2, 23, 0, 3079, 1, 1, 9, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1],
    [2, 130, 1, 1236, 1, 0, 11, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1],
    [2, 128, 0, 294, 2, 1, 107, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1],
    [2, 134, 1, 1769, 1, 1, 109, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 2],
    [1, 26, 0, 2733, 3, 1, 10, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0],
    [2, 124, 1, 281, 1, 0, 109, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0],
    [1, 123, 0, 2839, 2, 1, 10, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0],
    [2, 20, 0, 6292, 1, 0, 10, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1],
    [1, 130, 1, 5089, 3, 1, 11, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1],
    [2, 124, 0, 5153, 1, 0, 10, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1],
    [2, 119, 1, 3343, 2, 1, 10, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 2],
    [1, 129, 0, 1967, 1, 1, 10, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0],
    [2, 20, 0, 5031, 3, 1, 13, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 2],
    [1, 120, 1, 1656, 2, 1, 10, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 2]
])

# Realizar el Test de Friedman
stat, p_value = friedmanchisquare(data[:,0], data[:,1], data[:,2])

# Imprimir el resultado
print()
print("Estadístico de Friedman:", stat)
print()
print("Valor p:", p_value)
print()

# Evaluar el resultado
if p_value < 0.05:
    print("Los resultados son significativamente diferentes entre los algoritmos.")
    print()
else:
    print("No hay diferencias significativas entre los algoritmos.")
    print()