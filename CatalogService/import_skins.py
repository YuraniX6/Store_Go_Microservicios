#!/usr/bin/env python3
"""
Script para importar CSGO skins desde GitHub a Supabase
Descarga: https://raw.githubusercontent.com/ByMykel/CSGO-API/refs/heads/main/public/api/en/skins.json
"""

import json
import requests
import psycopg2
from psycopg2.extras import execute_values
from datetime import datetime
import sys

# Configuración de conexión
DB_CONFIG = {
    "host": "aws-1-us-east-2.pooler.supabase.com",
    "port": 6543,
    "database": "postgres",
    "user": "postgres.tzekxowuoleyzevqxopk",
    "password": "Lumadedouc25.",
}

GITHUB_URL = "https://raw.githubusercontent.com/ByMykel/CSGO-API/refs/heads/main/public/api/en/skins.json"


def fetch_skins_from_github():
    """Descarga el JSON de skins desde GitHub"""
    print("📥 Descargando skins desde GitHub...")
    try:
        response = requests.get(GITHUB_URL, timeout=30)
        response.raise_for_status()
        skins = response.json()
        print(f"✅ Se descargaron {len(skins)} skins")
        return skins
    except requests.exceptions.RequestException as e:
        print(f"❌ Error descargando del repositorio: {e}")
        sys.exit(1)


def connect_to_db():
    """Conecta a la base de datos Supabase"""
    print("🔌 Conectando a Supabase...")
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        print("✅ Conexión establecida")
        return conn
    except psycopg2.OperationalError as e:
        print(f"❌ Error conectando a la BD: {e}")
        sys.exit(1)


def get_existing_ids(conn):
    """Obtiene los IDs que ya existen en la BD"""
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM catalog_skins")
    existing_ids = {row[0] for row in cursor.fetchall()}
    cursor.close()
    return existing_ids


def transform_skin(skin):
    """Transforma el skin del JSON al formato de la BD"""
    now = datetime.utcnow().isoformat()
    
    # Extraer datos con valores por defecto
    skin_id = skin.get("id", "")
    name = skin.get("name", "")
    description = skin.get("description", None)
    image = skin.get("image", None)
    
    weapon = skin.get("weapon", {})
    weapon_id = weapon.get("id", None)
    weapon_name = weapon.get("name", None)
    
    category = skin.get("category", {})
    category_id = category.get("id", None)
    category_name = category.get("name", None)
    
    rarity = skin.get("rarity", {})
    rarity_id = rarity.get("id", None)
    rarity_name = rarity.get("name", None)
    rarity_color = rarity.get("color", None)
    
    # Manejar float limits
    float_limit = skin.get("float_limit", {})
    min_float = float_limit.get("min", None)
    max_float = float_limit.get("max", None)
    
    stattrak = skin.get("stattrak", False)
    souvenir = skin.get("souvenir", False)
    paint_index = skin.get("paint_index", None)
    legacy_model = skin.get("legacy_model", False)
    
    return {
        "id": skin_id,
        "name": name,
        "description": description,
        "image": image,
        "weapon_id": weapon_id,
        "weapon_name": weapon_name,
        "category_id": category_id,
        "category_name": category_name,
        "rarity_id": rarity_id,
        "rarity_name": rarity_name,
        "rarity_color": rarity_color,
        "min_float": min_float,
        "max_float": max_float,
        "stattrak": stattrak,
        "souvenir": souvenir,
        "paint_index": paint_index,
        "legacy_model": legacy_model,
        "raw_data": json.dumps(skin),
        "created_at": now,
        "updated_at": now,
    }


def insert_skins(conn, skins, existing_ids):
    """Inserta los skins en la BD, evitando duplicados"""
    cursor = conn.cursor()
    
    new_skins = []
    for skin in skins:
        skin_id = skin.get("id", "")
        if skin_id not in existing_ids:
            new_skins.append(transform_skin(skin))
    
    if not new_skins:
        print("ℹ️  No hay nuevas skins para insertar")
        cursor.close()
        return 0
    
    # Preparar valores para inserción
    values = [
        (
            s["id"],
            s["name"],
            s["description"],
            s["image"],
            s["weapon_id"],
            s["weapon_name"],
            s["category_id"],
            s["category_name"],
            s["rarity_id"],
            s["rarity_name"],
            s["rarity_color"],
            s["min_float"],
            s["max_float"],
            s["stattrak"],
            s["souvenir"],
            s["paint_index"],
            s["legacy_model"],
            s["raw_data"],
            s["created_at"],
            s["updated_at"],
        )
        for s in new_skins
    ]
    
    sql = """
    INSERT INTO catalog_skins (
        id, name, description, image, weapon_id, weapon_name,
        category_id, category_name, rarity_id, rarity_name, rarity_color,
        min_float, max_float, stattrak, souvenir, paint_index, legacy_model,
        raw_data, created_at, updated_at
    ) VALUES %s
    ON CONFLICT (id) DO NOTHING
    """
    
    try:
        execute_values(cursor, sql, values)
        conn.commit()
        print(f"✅ Se insertaron {len(new_skins)} nuevas skins")
        return len(new_skins)
    except psycopg2.Error as e:
        conn.rollback()
        print(f"❌ Error insertando skins: {e}")
        return 0
    finally:
        cursor.close()


def main():
    print("🚀 Iniciando importación de CSGO skins...\n")
    
    # Descargar skins
    skins = fetch_skins_from_github()
    
    # Conectar a BD
    conn = connect_to_db()
    
    # Obtener IDs existentes
    print("📊 Verificando skins existentes...")
    existing_ids = get_existing_ids(conn)
    print(f"ℹ️  {len(existing_ids)} skins ya en la BD")
    
    # Insertar nuevas skins
    print("\n💾 Insertando skins...")
    inserted = insert_skins(conn, skins, existing_ids)
    
    # Cerrar conexión
    conn.close()
    
    print(f"\n✨ Importación completada!")
    print(f"   Total en GitHub: {len(skins)}")
    print(f"   Ya existentes: {len(existing_ids)}")
    print(f"   Insertadas: {inserted}")


if __name__ == "__main__":
    main()
