EXECUTIONS_PER_THREAD = 100


def make_assertions(cpu_usage, all_data):
    print("Making assertions for each user priority...")
    for key, thread_data in sorted(all_data.items()):
        _assert_total_executions(thread_data)

    print("Making assertions for CPU usage...")
    _assert_cpu_usage(cpu_usage, len(all_data))

def _assert_total_executions(thread_data):
    total_execution_operations = (
        thread_data["total_offloading"] + thread_data["total_local_execution"]
    )

    if total_execution_operations != EXECUTIONS_PER_THREAD:
        print("Wrong number of executions: {}".format(total_execution_operations))
        exit(1)
    else:
        print("Number of executions is okay")

def _assert_cpu_usage(cpu_usage, total_threads):
    expected_items = EXECUTIONS_PER_THREAD * total_threads
    if len(cpu_usage) != expected_items:
        print("Wrong number of cpu usage requests: {}".format(len(cpu_usage)))
        exit(1)
    else:
        print("Number of cpu usage requests is okay")
