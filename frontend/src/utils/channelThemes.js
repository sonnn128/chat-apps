// Predefined channel themes (inspired by Messenger)
export const CHANNEL_THEMES = {
  // Solid Colors
  BLUE: {
    id: 'blue',
    name: 'Blue',
    type: 'color',
    color: '#0084FF',
    gradient: null,
  },
  PURPLE: {
    id: 'purple',
    name: 'Purple',
    type: 'color',
    color: '#A033FF',
    gradient: null,
  },
  GREEN: {
    id: 'green',
    name: 'Green',
    type: 'color',
    color: '#00C851',
    gradient: null,
  },
  ORANGE: {
    id: 'orange',
    name: 'Orange',
    type: 'color',
    color: '#FF6900',
    gradient: null,
  },
  RED: {
    id: 'red',
    name: 'Red',
    type: 'color',
    color: '#FA3E3E',
    gradient: null,
  },
  PINK: {
    id: 'pink',
    name: 'Pink',
    type: 'color',
    color: '#FF69B4',
    gradient: null,
  },
  
  // Gradients
  SUNSET: {
    id: 'sunset',
    name: 'Sunset',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #FF6B6B 0%, #FFD93D 100%)',
  },
  OCEAN: {
    id: 'ocean',
    name: 'Ocean',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  CANDY: {
    id: 'candy',
    name: 'Candy',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  FIRE: {
    id: 'fire',
    name: 'Fire',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  },
  MINT: {
    id: 'mint',
    name: 'Mint',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  },
  PEACH: {
    id: 'peach',
    name: 'Peach',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  },
  LAVENDER: {
    id: 'lavender',
    name: 'Lavender',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  },
  AURORA: {
    id: 'aurora',
    name: 'Aurora',
    type: 'gradient',
    color: null,
    gradient: 'linear-gradient(135deg, #13547a 0%, #80d0c7 100%)',
  },
};

// Get theme by ID
export const getThemeById = (themeId) => {
  return Object.values(CHANNEL_THEMES).find(theme => theme.id === themeId) || CHANNEL_THEMES.BLUE;
};

// Get theme from channel data
export const getChannelTheme = (channel) => {
  if (!channel) return CHANNEL_THEMES.BLUE;

  // console.log("🔍 getChannelTheme check:", { 
  //     id: channel.id, 
  //     color: channel.themeColor, 
  //     gradient: channel.themeGradient 
  // });
  
  // If channel has custom gradient
  if (channel.themeGradient) {
    return {
      id: 'custom',
      name: 'Custom',
      type: 'gradient',
      color: null,
      gradient: channel.themeGradient,
    };
  }
  
  // If channel has custom color
  if (channel.themeColor) {
    // Try to find matching predefined theme
    const matchingTheme = Object.values(CHANNEL_THEMES).find(
      theme => theme.color === channel.themeColor
    );
    
    if (matchingTheme) return matchingTheme;
    
    // Return custom color theme
    return {
      id: 'custom',
      name: 'Custom',
      type: 'color',
      color: channel.themeColor,
      gradient: null,
    };
  }
  
  // Default theme
  return CHANNEL_THEMES.BLUE;
};

// Get CSS style for theme
export const getThemeStyle = (theme) => {
  if (theme.type === 'gradient') {
    return {
      background: theme.gradient,
    };
  }
  return {
    backgroundColor: theme.color,
  };
};
