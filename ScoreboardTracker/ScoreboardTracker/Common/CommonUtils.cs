namespace ScoreboardTracker.Common
{
    public class CommonUtils
    {
        public static int ZeroIfEmpty(string value)
        {
            if (string.IsNullOrWhiteSpace(value))
            {
                return 0;
            }

            int.TryParse(value, out int intValue);
            return intValue;
        }

        public static int? NullIfEmpty(string value)
        {
            if (string.IsNullOrWhiteSpace(value))
            {
                return null;
            }

            int.TryParse(value, out int intValue);
            return intValue;
        }
    }
}
