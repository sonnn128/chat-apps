import { toast } from "react-hot-toast";

export const successToast = (message) => {
  toast.success(message, {
    duration: 3000,
    position: "top-center",
    style: {
      fontSize: "14px",
    },
  });
};
export const errorToast = (message) => {
  toast.error(message, {
    duration: 3000,
    position: "top-center",
    style: {
      fontSize: "14px",
    },
  });
};

export const warningToast = (message) => {
  toast.error(message, {
    duration: 3000,
    position: "top-center",
    style: {
      fontSize: "14px",
    },
  });
};

export const infoToast = (message) => {
  toast.error(message, {
    duration: 3000,
    position: "top-center",
    style: {
      fontSize: "14px",
    },
  });
};

export const defaultToast = (message) => {
  toast.error(message, {
    duration: 3000,
    position: "top-center",
    style: {
      fontSize: "14px",
    },
  });
};
