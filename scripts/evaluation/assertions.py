import logging

EXECUTIONS_PER_THREAD = 100


def make_assertions(cpu_usage, all_data):
    logging.info("Making assertions for each user priority...")
    for key, thread_data in sorted(all_data.items()):
        for fog_node_name in thread_data["fog_nodes_data"]:
            fog_data = thread_data["fog_nodes_data"][fog_node_name]
            _assert_total_executions(fog_data)

    logging.info("Making assertions for CPU usage...")
    for key, current_cpu_node_data in cpu_usage.items():
        _assert_cpu_usage(current_cpu_node_data, len(all_data))


def _assert_total_executions(thread_data):
    total_execution_operations = (
        thread_data["total_offloading"] + thread_data["total_local_execution"]
    )

    if total_execution_operations != EXECUTIONS_PER_THREAD:
        logging.info(
            "Wrong number of executions: {}".format(total_execution_operations)
        )
        exit(1)
    else:
        logging.info("Number of executions is okay")


def _assert_cpu_usage(cpu_usage, total_threads):
    expected_items = EXECUTIONS_PER_THREAD * total_threads
    if len(cpu_usage) != expected_items:
        logging.info("Wrong number of cpu usage requests: {}".format(len(cpu_usage)))
        exit(1)
    else:
        logging.info("Number of cpu usage requests is okay")
