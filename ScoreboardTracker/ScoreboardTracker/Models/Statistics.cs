using FFImageLoading.Forms;

namespace ScoreboardTracker.Models
{
    public class Statistics
    {
        public User user { get; set; }
        public string statisticsHeader { get; set; }
        public string statisticsValue { get; set; }

        public DataUrlImageSource ImageSource => new DataUrlImageSource(user?.profileUrl ?? "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSf_Bf0-x44hsGqqcQwrTcNeLUSnYjlDuoql-hQHydDdBwxeCT2)");
    }
}
