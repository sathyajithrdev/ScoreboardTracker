using System.Threading.Tasks;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface IPage
    {
        Task DisplayAlert(string message);
        void ShowToast(string message);
    }
}
