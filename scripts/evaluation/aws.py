import logging
import boto3
import os


def upload_public_file_to_s3_as_bytes(file_bytes, bucket, object_name):
    _s3_client().put_object(
        Body=file_bytes,
        Bucket=bucket,
        Key=object_name,
        ContentType="binary/octet-stream",
        ACL="public-read",
    )


def locate_vm_data_with_name(name_mask):
    all_vms = []
    response = _ec2_client().describe_instances(
        Filters=[{"Name": "tag:Name", "Values": [name_mask]}]
    )
    for r in response["Reservations"]:
        for i in r["Instances"]:
            current_vm_info = {}
            current_vm_info["name"] = _get_name_tag_value(i["Tags"])
            current_vm_info["public_ip"] = i["PublicDnsName"]
            current_vm_info["private_ip"] = i["PrivateDnsName"]
            all_vms.append(current_vm_info)

    logging.info(
        "Running VMs for nodes with name mask {}: {}".format(name_mask, all_vms)
    )
    return all_vms


def _get_name_tag_value(tags):
    for tag in tags:
        if tag["Key"] == "Name":
            return tag["Value"]

    logging.error("Could not find the name of the VM")
    exit(1)


def _ec2_client():
    return _boto3_client("ec2")


def _s3_client():
    return _boto3_client("s3")


def _boto3_client(service_name):
    return boto3.client(
        service_name,
        aws_access_key_id=os.environ.get("ACCESS_KEY"),
        aws_secret_access_key=os.environ.get("SECRET_KEY"),
        region_name="sa-east-1",
    )
