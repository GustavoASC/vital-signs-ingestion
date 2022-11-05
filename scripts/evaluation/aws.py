import logging
import boto3
import os


def locate_vm_ips():
    all_fog_nodes = []
    response = _ec2_client().describe_instances(
        Filters=[{"Name": "tag:Name", "Values": ["fog_node_a"]}]
    )
    for r in response["Reservations"]:
        for i in r["Instances"]:
            all_fog_nodes.append(i["PublicDnsName"])

    logging.info("IPs for running fog nodes: {}".format(all_fog_nodes))
    return all_fog_nodes


def _ec2_client():
    return boto3.client(
        "ec2",
        aws_access_key_id=os.environ.get("ACCESS_KEY"),
        aws_secret_access_key=os.environ.get("SECRET_KEY"),
        region_name="sa-east-1",
    )
