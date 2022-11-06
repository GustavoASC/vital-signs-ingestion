import logging
import boto3
import os


def locate_vm_ips_with_name(name):
    all_ips = []
    response = _ec2_client().describe_instances(
        Filters=[{"Name": "tag:Name", "Values": [name]}]
    )
    for r in response["Reservations"]:
        for i in r["Instances"]:
            current_vm_info = {}
            current_vm_info["public_ip"] = i["PublicDnsName"]
            current_vm_info["private_ip"] = i["PrivateDnsName"]
            all_ips.append(current_vm_info)

    logging.info("IPs for running for nodes with name {}: {}".format(name, all_ips))
    return all_ips


def _ec2_client():
    return boto3.client(
        "ec2",
        aws_access_key_id=os.environ.get("ACCESS_KEY"),
        aws_secret_access_key=os.environ.get("SECRET_KEY"),
        region_name="sa-east-1",
    )
