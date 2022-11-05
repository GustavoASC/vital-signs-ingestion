EXECUTIONS_PER_THREAD = 100


def make_assertions(all_data):
    print("Making assertions...")
    for key, thread_data in sorted(all_data.items()):
        _assert_total_executions(thread_data)


def _assert_total_executions(thread_data):
    total_execution_operations = (
        thread_data["total_offloading"] + thread_data["total_local_execution"]
    )

    if total_execution_operations != EXECUTIONS_PER_THREAD:
        print("Wrong number of executions: {}".format(total_execution_operations))
        exit(1)
    else:
        print("Number of executions is okay")
