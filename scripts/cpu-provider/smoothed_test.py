import unittest
import smoothed


class TestSum(unittest.TestCase):
    def test_aging_paper(self):
        data = [82, 78, 81, 80, 77, 93]
        result = smoothed.aging(data)
        self.assertEqual(result, 84.53125)

    def test_aging_thesis(self):
        data = [70, 85, 75, 40, 20, 60]
        result = smoothed.aging(data)
        self.assertEqual(result, 48.4375)


if __name__ == "__main__":
    unittest.main()
