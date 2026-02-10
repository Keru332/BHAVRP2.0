import random

def generate_bhavrp_instance(file_path="large_instance.dat", num_customers=1500, num_depots=10, vehicle_capacity=80, seed=42):
    random.seed(seed)
    lines = []

    # Cabecera
    lines.append(f"{num_customers} {vehicle_capacity} {num_depots}")

    # Capacidades por depósito
    for _ in range(num_depots):
        lines.append(str(vehicle_capacity))

    # Generar clientes
    for customer_id in range(1, num_customers + 1):
        x = random.randint(0, 100)
        y = random.randint(0, 100)
        demand = random.randint(1, 30)
        lines.append(f"{customer_id} {x} {y} {demand}")

    # Generar depósitos (continuación de ids)
    for depot_id in range(num_customers + 1, num_customers + num_depots + 1):
        x = random.randint(0, 100)
        y = random.randint(0, 100)
        lines.append(f"{depot_id} {x} {y}")

    # Guardar archivo
    with open(file_path, "w") as f:
        f.write("\n".join(lines))

    print(f"✅ Instancia generada exitosamente en '{file_path}' con {num_customers} clientes y {num_depots} depósitos.")

if __name__ == "__main__":
    generate_bhavrp_instance()
