import aws
import logging

def update_properties(fog_nodes, cloud_api_adapter_url):
    logging.info("Generating nodes-mappings.properties with nodes configuration...")

    relationship = {
        "fog_node_a": "fog_node_e",
        "fog_node_b": "fog_node_e",
        "fog_node_c": "fog_node_f",
        "fog_node_d": "fog_node_f",
        "fog_node_e": "fog_node_g",
        "fog_node_f": "fog_node_g",
        "fog_node_g": "cloud_api_adapter"
    }

    lines = []
    for key, value in relationship.items():
        lines.append("connection.{}={}".format(key, value))

    for current_fog_node in fog_nodes:
        name = current_fog_node["name"]
        public_ip = current_fog_node["public_ip"]
        lines.append("ip_address.{}={}".format(name, public_ip))

    lines.append("ip_address.cloud_api_adapter={}".format(cloud_api_adapter_url))

    single_string = ""
    for current_line in lines:
        single_string = single_string + "\n" + current_line

    logging.info("Mapping has the following content:")
    logging.info(single_string)

    aws.upload_public_file_to_s3_as_bytes(
        file_bytes = single_string.encode('utf-8'),
        bucket = "nodes-mappings",
        object_name = "nodes-mappings.properties"
    )